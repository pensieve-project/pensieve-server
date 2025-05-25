package ru.hse.pensieve.subscriptions;

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
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.posts.service.PostService;
import ru.hse.pensieve.profiles.service.ProfileService;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;
import ru.hse.pensieve.subscriptions.service.SubscriptionsService;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Disabled
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
public class SubscriptionsIntegrationTest {

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

    private KafkaConsumer<String, Post> postKafkaConsumer;
    private KafkaConsumer<String, SubscriptionRequest> subscriptionsKafkaConsumer;
    private KafkaConsumer<String, UUID> vipKafkaConsumer;
    private final String POST_TOPIC = "post-created";
    private final String USER_SUBSCRIBED_TOPIC = "user-subscribed";
    private final String USER_UNSUBSCRIBED_TOPIC = "user-unsubscribed";
    private final String USER_BECAME_VIP_TOPIC = "user-became-vip";
    private final String USER_STOP_VIP_TOPIC = "user-stop-vip";

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
    private ProfileService profileService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private SubscriptionsService subscriptionsService;

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

        Properties postProps = new Properties();
        postProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        postProps.put(ConsumerConfig.GROUP_ID_CONFIG, "posts-test-group");
        postProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        postProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        postProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        postProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        postKafkaConsumer = new KafkaConsumer<>(postProps);
        postKafkaConsumer.subscribe(new ArrayList<>(List.of(POST_TOPIC)));

        Properties subsProps = new Properties();
        subsProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        subsProps.put(ConsumerConfig.GROUP_ID_CONFIG, "subs-test-group");
        subsProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        subsProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        subsProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        subsProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        subscriptionsKafkaConsumer = new KafkaConsumer<>(subsProps);
        subscriptionsKafkaConsumer.subscribe(new ArrayList<>(List.of(USER_SUBSCRIBED_TOPIC, USER_UNSUBSCRIBED_TOPIC)));

        Properties vipProps = new Properties();
        vipProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        vipProps.put(ConsumerConfig.GROUP_ID_CONFIG, "vip-test-group");
        vipProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        vipProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        vipProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        vipProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        vipKafkaConsumer = new KafkaConsumer<>(vipProps);
        vipKafkaConsumer.subscribe(new ArrayList<>(List.of(USER_BECAME_VIP_TOPIC, USER_STOP_VIP_TOPIC)));
    }

    @AfterEach
    void cleanupKafka() {
        postKafkaConsumer.close();
        subscriptionsKafkaConsumer.close();
        vipKafkaConsumer.close();
    }

    @Test
    void userBecameVip() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscribers = new ArrayList<>();
        for (int i = 0; i < subscriptionsService.vipBound; i++) {
            UUID subscriberId = UUID.randomUUID();
            Profile profile = createProfile(subscriberId);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(subscriberId, userId));
            subscribers.add(subscriberId);
        }
        assertEquals(subscriptionsService.vipBound, subscriptionsService.getSubscribersCount(userId));

        assertFalse(profileRepository.isVip(userId));

        int postsCount = 5;
        List<UUID> posts = new ArrayList<>();
        for (int i = 0; i < postsCount; i++) {
            PostResponse post = postService.savePost(createPostRequest(userId));
            posts.add(post.getPostId());
            assertPostInKafka(post.getPostId());
            Thread.sleep(1000);
        }

        Thread.sleep(15000);

        for (UUID subscriber : subscribers) {
            assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(subscriber, userId)));
            List<UUID> feedPosts = new ArrayList<>();
            for (int bucket = 0; bucket < 10; bucket++) {
                feedPosts.addAll(userFeedRepository.findAllByUserIdAndBucket(subscriber, bucket).stream().filter(f -> f.getAuthorId().equals(userId)).map(u -> u.getKey().getPostId()).toList());
            }
            assertEquals(postsCount, feedPosts.size());
        }

        UUID subscriberId = UUID.randomUUID();
        Profile profile = createProfile(subscriberId);
        profileRepository.save(profile);
        subscriptionsService.subscribe(new SubscriptionRequest(subscriberId, userId));
        assertBecameVipInKafka(userId);
        subscribers.add(subscriberId);
        assertTrue(profileRepository.isVip(userId));

        Thread.sleep(30000);

        for (UUID subscriber : subscribers) {
            List<UUID> feedPosts = new ArrayList<>();
            for (int bucket = 0; bucket < 10; bucket++) {
                feedPosts.addAll(userFeedRepository.findAllByUserIdAndBucket(subscriber, bucket).stream().filter(f -> f.getAuthorId().equals(userId)).map(u -> u.getKey().getPostId()).toList());
            }
            assertTrue(feedPosts.isEmpty(), "Found " + feedPosts.size() + " vip posts.");
        }

        for (UUID post : posts) {
            assertNotNull(redisService.getVipPost(userId, post));
        }

        for (UUID subscriber : subscribers) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(subscriber, postsCount, null);
            assertEquals(postsCount, feed.size());
            assertTrue(feed.stream().allMatch(post -> post.getAuthorId().equals(userId)));
            assertEquals(posts.stream().sorted().toList(), feed.stream().map(PostResponse::getPostId).sorted().toList());
        }
    }

    @Test
    void userStopVip() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscribers = new ArrayList<>();
        for (int i = 0; i < subscriptionsService.vipBound + 1; i++) {
            UUID subscriberId = UUID.randomUUID();
            Profile profile = createProfile(subscriberId);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(subscriberId, userId));
            subscribers.add(subscriberId);
        }
        assertEquals(subscriptionsService.vipBound + 1, subscriptionsService.getSubscribersCount(userId));

        assertTrue(profileRepository.isVip(userId));

        int postsCount = 5;
        List<UUID> posts = new ArrayList<>();
        for (int i = 0; i < postsCount; i++) {
            PostResponse post = postService.savePost(createPostRequest(userId));
            posts.add(post.getPostId());
            assertPostNotInKafka(post.getPostId());
            assertNotNull(redisService.getVipPost(userId, post.getPostId()));
            Thread.sleep(1000);
        }

        for (UUID subscriber : subscribers) {
            assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(subscriber, userId)));
            List<UUID> feedPosts = new ArrayList<>();
            for (int bucket = 0; bucket < 10; bucket++) {
                feedPosts.addAll(userFeedRepository.findAllByUserIdAndBucket(subscriber, bucket).stream().filter(f -> f.getAuthorId().equals(userId)).map(u -> u.getKey().getPostId()).toList());
            }
            assertTrue(feedPosts.isEmpty(), "Found " + feedPosts.size() + " vip posts.");
        }

        UUID subscriberId = subscribers.get(new Random().nextInt(subscribers.size()));
        subscriptionsService.unsubscribe(new SubscriptionRequest(subscriberId, userId));
        assertStopVipInKafka(userId);
        subscribers.remove(subscriberId);
        assertFalse(profileRepository.isVip(userId));

        Thread.sleep(50000);

        assertTrue(feedService.getSubscriptionsFeed(subscriberId, postsCount, null).isEmpty());

        for (UUID subscriber : subscribers) {
            List<UUID> feedPosts = new ArrayList<>();
            for (int bucket = 0; bucket < 10; bucket++) {
                feedPosts.addAll(userFeedRepository.findAllByUserIdAndBucket(subscriber, bucket).stream().filter(f -> f.getAuthorId().equals(userId)).map(u -> u.getKey().getPostId()).toList());
            }
            assertEquals(postsCount, feedPosts.size());
        }

        for (UUID post : posts) {
            assertNull(redisService.getVipPost(userId, post));
        }

        for (UUID subscriber : subscribers) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(subscriber, postsCount, null);
            assertEquals(postsCount, feed.size());
            assertTrue(feed.stream().allMatch(post -> post.getAuthorId().equals(userId)));
            assertEquals(posts.stream().sorted().toList(), feed.stream().map(PostResponse::getPostId).sorted().toList());
        }
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

    private void assertPostInKafka(UUID postId) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, Post> records = postKafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(POST_TOPIC)
                            && r.value().getKey().getPostId().equals(postId));
        });
    }

    private void assertPostNotInKafka(UUID postId) {
        boolean found = true;
        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> {
                ConsumerRecords<String, Post> records = postKafkaConsumer.poll(Duration.ofMillis(100));
                return StreamSupport.stream(records.spliterator(), false)
                        .noneMatch(r -> r.topic().equals(POST_TOPIC)
                                && r.value().getKey().getPostId().equals(postId));
            });
            found = false;
        } catch (ConditionTimeoutException ignored) {
        }
        assertFalse(found, "Post " + postId + " найден в топике " + POST_TOPIC);
    }

    private void assertSubscriptionInKafka(UUID subscriberId) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, SubscriptionRequest> records = subscriptionsKafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(USER_SUBSCRIBED_TOPIC)
                            && r.value().getSubscriberId().equals(subscriberId));
        });
    }

    private void assertLostSubscriptionInKafka(UUID subscriberId) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, SubscriptionRequest> records = subscriptionsKafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(USER_UNSUBSCRIBED_TOPIC)
                            && r.value().getSubscriberId().equals(subscriberId));
        });
    }

    private void assertBecameVipInKafka(UUID userId) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, UUID> records = vipKafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(USER_BECAME_VIP_TOPIC)
                            && r.value().equals(userId));
        });
    }

    private void assertStopVipInKafka(UUID userId) {
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            ConsumerRecords<String, UUID> records = vipKafkaConsumer.poll(Duration.ofMillis(100));
            return StreamSupport.stream(records.spliterator(), false)
                    .anyMatch(r -> r.topic().equals(USER_STOP_VIP_TOPIC)
                            && r.value().equals(userId));
        });
    }
}