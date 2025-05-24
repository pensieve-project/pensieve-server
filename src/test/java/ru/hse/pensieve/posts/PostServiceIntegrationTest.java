package ru.hse.pensieve.posts;

import com.redis.testcontainers.RedisContainer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.core.ConditionTimeoutException;
import org.testcontainers.utility.DockerImageName;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.service.PostService;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.enabled=false",
        "spring.data.jpa.repositories.enabled=false"
})
public class PostServiceIntegrationTest {

    @Container
    public static CassandraContainer<?> cassandra =
            new CassandraContainer<>("cassandra:latest")
                    .withExposedPorts(9042)
                    .withStartupTimeout(Duration.ofMinutes(2));

    @Container
    private static RedisContainer redis =
            new RedisContainer(DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379);

    @Container
    public static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
                    .withEmbeddedZookeeper();

    private KafkaConsumer<String, Post> kafkaConsumer;

    @DynamicPropertySource
    public static void setupProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.cassandra.contact-points",
                () -> cassandra.getHost() + ":" + cassandra.getMappedPort(9042));
        registry.add("spring.data.cassandra.local-datacenter",
                () -> "datacenter1");
        registry.add("spring.data.cassandra.request.timeout", () -> "20s");
        registry.add("spring.data.cassandra.connect.timeout", () -> "20s");
        registry.add("spring.data.cassandra.read-timeout", () -> "20s");

        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private PostService postService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        try (var session = cassandra.getCluster().connect()) {
            session.execute("CREATE KEYSPACE IF NOT EXISTS pensieve "
                    + "WITH replication = {'class':'SimpleStrategy', 'replication_factor':'1'};");
        }

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(new ArrayList<>(List.of("post-created", "user-subscribed", "user-unsubscribed", "user-became-vip", "user-stop-vip")));
    }

    @AfterEach
    void cleanupKafka() {
        kafkaConsumer.close();
    }

    @Test
    void testCreateAndRetrievePost() {
        UUID authorId = UUID.randomUUID();
        profileRepository.save(createProfile(authorId));
        UUID themeId = UUID.randomUUID();

        PostRequest request = createPostRequest(authorId, themeId);
        PostResponse response = postService.savePost(request);

        assertNotNull(response.getPostId());
        assertEquals(authorId, response.getAuthorId());
        assertEquals(themeId, response.getThemeId());

        Optional<Post> post = postRepository.findById(new PostKey(themeId, response.getTimeStamp(), authorId, response.getPostId()));
        assertTrue(post.isPresent());
    }

    @Test
    void testLikePost() {
        UUID authorId = UUID.randomUUID();
        profileRepository.save(createProfile(authorId));

        PostRequest postRequest = createPostRequest(authorId);
        PostResponse postResponse = postService.savePost(postRequest);

        LikeRequest likeRequest = new LikeRequest(authorId, postResponse.getPostId());
        postService.likePost(likeRequest);

        assertTrue(postService.hasUserLikedPost(likeRequest));
        assertEquals(1, postService.getLikesCount(postResponse.getPostId()));

        postService.unlikePost(likeRequest);

        assertFalse(postService.hasUserLikedPost(likeRequest));
        assertEquals(0, postService.getLikesCount(postResponse.getPostId()));
    }

    @Test
    void testCommentOperations() {
        UUID authorId = UUID.randomUUID();
        profileRepository.save(createProfile(authorId));

        PostRequest postRequest = createPostRequest(authorId);
        PostResponse post = postService.savePost(postRequest);

        CommentRequest commentRequest = new CommentRequest(
                post.getPostId(),
                authorId,
                "Great post!"
        );
        CommentResponse comment = postService.leaveComment(commentRequest);

        assertEquals(1, postService.getCommentsCount(post.getPostId()));
        List<CommentResponse> comments = postService.getPostComments(post.getPostId());
        assertEquals(1, comments.size());
        assertEquals("Great post!", comments.getFirst().getText());
    }

    @Test
    void testNonVipUserPostKafkaEvent() {
        UUID userId = UUID.randomUUID();
        Profile profile = createProfile(userId);
        profile.setIsVip(false);
        profileRepository.save(profile);

        PostRequest request = createPostRequest(userId);
        PostResponse response = postService.savePost(request);

        assertNotNull(response);

        assertPostInKafka(response.getPostId(), "post-created");

        Optional<Post> post = postRepository.findById(new PostKey(response.getThemeId(), response.getTimeStamp(), userId, response.getPostId()));
        assertTrue(post.isPresent());

        assertNull(redisService.getVipPost(userId, response.getPostId()));
    }

    @Test
    void testVipUserPostRedisCaching() {
        UUID vipUserId = UUID.randomUUID();
        Profile vipProfile = createProfile(vipUserId);
        vipProfile.setIsVip(true);
        profileRepository.save(vipProfile);

        PostRequest request = createPostRequest(vipUserId);
        PostResponse response = postService.savePost(request);

        assertNotNull(response);

        System.out.println(response.getPostId());

        assertPostNotInKafka(response.getPostId(), "post-created");

        Optional<Post> post = postRepository.findById(new PostKey(response.getThemeId(), response.getTimeStamp(), vipUserId, response.getPostId()));
        assertTrue(post.isPresent());

        assertNotNull(redisService.getVipPost(vipUserId, response.getPostId()));
    }

    @Test
    void testBecameVip() {
        UUID userId = UUID.randomUUID();
        Profile profile = createProfile(userId);
        profile.setIsVip(false);
        profileRepository.save(profile);

        PostRequest request = createPostRequest(userId);
        PostResponse notVipPost = postService.savePost(request);

        assertNull(redisService.getVipPost(userId, notVipPost.getPostId()));

        assertPostInKafka(notVipPost.getPostId(), "post-created");

        profile.setIsVip(true);
        profileRepository.save(profile);

        PostResponse vipPost = postService.savePost(request);

        assertNull(redisService.getVipPost(userId, notVipPost.getPostId()));
        assertNotNull(redisService.getVipPost(userId, vipPost.getPostId()));

        System.out.println(notVipPost.getPostId());
        System.out.println(vipPost.getPostId());

        assertPostNotInKafka(vipPost.getPostId(), "post-created");
    }

    @Test
    void testStopVip() {
        UUID userId = UUID.randomUUID();
        Profile profile = createProfile(userId);
        profile.setIsVip(true);
        profileRepository.save(profile);

        PostRequest request = createPostRequest(userId);
        PostResponse vipPost = postService.savePost(request);

        System.out.println(vipPost.getPostId());

        assertPostNotInKafka(vipPost.getPostId(), "post-created");

        profile.setIsVip(false);
        profileRepository.save(profile);

        PostResponse notVipPost = postService.savePost(request);

        System.out.println(notVipPost.getPostId());

        assertNull(redisService.getVipPost(userId, notVipPost.getPostId()));
        assertNotNull(redisService.getVipPost(userId, vipPost.getPostId()));

        assertPostInKafka(notVipPost.getPostId(), "post-created");

    }

    @Test
    void testPostWithCoAuthorsAndAlbumCreation() {
        UUID authorId = UUID.randomUUID();
        UUID coAuthor1 = UUID.randomUUID();
        UUID coAuthor2 = UUID.randomUUID();

        PostRequest request = createPostRequest(authorId);
        request.setCoAuthors(Set.of(coAuthor1, coAuthor2));

        PostResponse response = postService.savePost(request);

        Set<UUID> expectedCoAuthors = new TreeSet<>(Set.of(authorId, coAuthor1, coAuthor2));
        Optional<Album> album = albumRepository.findById(new AlbumKey(authorId, expectedCoAuthors));
        assertTrue(album.isPresent());

        assertTrue(albumRepository.findById(new AlbumKey(coAuthor1, expectedCoAuthors)).isPresent());
        assertTrue(albumRepository.findById(new AlbumKey(coAuthor2, expectedCoAuthors)).isPresent());
    }

    private PostRequest createPostRequest(UUID authorId) {
        return new PostRequest(
                "Test post",
                new MockMultipartFile("photo", "test.jpg", "image/jpeg", "content".getBytes()),
                null,
                authorId,
                UUID.randomUUID(),
                null
        );
    }

    private PostRequest createPostRequest(UUID authorId, UUID themeId) {
        return new PostRequest(
                "Test post",
                new MockMultipartFile("photo", "test.jpg", "image/jpeg", "content".getBytes()),
                null,
                authorId,
                themeId,
                null
        );
    }

    private Post createPost(UUID authorId) {
        PostKey key = new PostKey(UUID.randomUUID(), Instant.now(), authorId, UUID.randomUUID());
        return new Post(key, ByteBuffer.wrap(new byte[0]), "Test", null, Set.of(), null, 0, 0);
    }

    private Post createPost(UUID authorId, UUID themeId) {
        PostKey key = new PostKey(themeId, Instant.now(), authorId, UUID.randomUUID());
        return new Post(key, ByteBuffer.wrap(new byte[0]), "Test", null, Set.of(), null, 0, 0);
    }

    private Profile createProfile(UUID authorId) {
        return new Profile(
                authorId,
                null,
                "description",
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                0,
                false
        );
    }

    private void assertPostInKafka(UUID postId, String topic) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, Post> records = kafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(topic)
                            && r.value().getKey().getPostId().equals(postId));
        });
    }

    private void assertPostNotInKafka(UUID postId, String topic) {
        boolean found = true;
        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> {
                ConsumerRecords<String, Post> records = kafkaConsumer.poll(Duration.ofMillis(100));
                return !StreamSupport.stream(records.spliterator(), false)
                        .anyMatch(r -> r.topic().equals(topic)
                                && r.value().getKey().getPostId().equals(postId));
            });
            found = false;
        } catch (ConditionTimeoutException ignored) {
        }
        assertFalse(found, "Post " + postId + " найден в топике " + topic);
    }
}