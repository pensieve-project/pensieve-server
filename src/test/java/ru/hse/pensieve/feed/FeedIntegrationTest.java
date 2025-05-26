package ru.hse.pensieve.feed;

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
public class FeedIntegrationTest {

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
    private final String POST_TOPIC = "post-created";
    private final String USER_SUBSCRIBED_TOPIC = "user-subscribed";
    private final String USER_UNSUBSCRIBED_TOPIC = "user-unsubscribed";

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
    }

    @AfterEach
    void cleanupKafka() {
        postKafkaConsumer.close();
        subscriptionsKafkaConsumer.close();
    }

    @Test
    void getSimpleUserFeed() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscriptions = new ArrayList<>();
        int vipSubscriptionsCount = 10;
        for (int i = 0; i < vipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(true);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
        }
        int notVipSubscriptionsCount = 20;
        for (int i = 0; i < notVipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(false);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
            assertSubscriptionInKafka(userId);
        }
        assertEquals(vipSubscriptionsCount + notVipSubscriptionsCount, subscriptions.size());

        Thread.sleep(1000);

        int postsPerAuthorCount = 3;
        for (int i = 0; i < postsPerAuthorCount; i++) {
            for (UUID subscription : subscriptions) {
                PostResponse post = postService.savePost(createPostRequest(subscription));
                assertNotNull(post);
                if (profileRepository.isVip(subscription)) {
                    assertNotNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostNotInKafka(post.getPostId());
                } else {
                    assertNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostInKafka(post.getPostId());
                    Thread.sleep(100);
                    boolean found = false;
                    for (int bucket = 0; bucket < 10; bucket++) {
                        if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getKey().getPostId().equals(post.getPostId()))) {
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
                Thread.sleep(100);
            }
        }

        Thread.sleep(1000);

        int feedLimit = 5;
        Instant lastSeenTime = null;
        Set<UUID> seenPosts = new HashSet<>();
        int expectedPostsCount = postsPerAuthorCount * subscriptions.size();
        int feedParts = (int) Math.ceil((double) (postsPerAuthorCount * subscriptions.size()) / feedLimit);
        for (int i = 0; i < feedParts; i++) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
            assertNotNull(feed);

            for (PostResponse post : feed) {
                assertNotNull(post);
                assertTrue(seenPosts.add(post.getPostId()));
                assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(userId, post.getAuthorId())));
            }

            try {
                lastSeenTime = feed.getLast().getTimeStamp();
            } catch (NoSuchElementException e) {
                fail("Потеряно " + (expectedPostsCount - seenPosts.size()) + " постов.");
            }
        }
        List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
        assertTrue(feed.isEmpty());

        assertEquals(postsPerAuthorCount * subscriptions.size(), seenPosts.size());
    }

    @Test
    void getUserFeedAfterSubscription() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscriptions = new ArrayList<>();
        int vipSubscriptionsCountFirst = 10;
        for (int i = 0; i < vipSubscriptionsCountFirst; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(true);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
        }
        int notVipSubscriptionsCountFirst = 20;
        for (int i = 0; i < notVipSubscriptionsCountFirst; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(false);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
            assertSubscriptionInKafka(userId);
        }

        int postsPerAuthorCount = 3;
        for (int i = 0; i < postsPerAuthorCount; i++) {
            for (UUID subscription : subscriptions) {
                PostResponse post = postService.savePost(createPostRequest(subscription));
                if (profileRepository.isVip(subscription)) {
                    assertNotNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostNotInKafka(post.getPostId());
                } else {
                    assertNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostInKafka(post.getPostId());
                    Thread.sleep(100);
                    boolean found = false;
                    for (int bucket = 0; bucket < 10; bucket++) {
                        if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getKey().getPostId().equals(post.getPostId()))) {
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
                Thread.sleep(100);
            }
        }

        List<UUID> additionalSubscriptions = new ArrayList<>();
        int vipSubscriptionsCountSecond = 10;
        for (int j = 0; j < vipSubscriptionsCountSecond; j++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(true);
            profileRepository.save(profile);

            additionalSubscriptions.add(subscriptionId);

            for (int i = 0; i < postsPerAuthorCount; i++) {
                PostResponse post = postService.savePost(createPostRequest(subscriptionId));
                assertNotNull(post);
                assertNotNull(redisService.getVipPost(subscriptionId, post.getPostId()));
                assertPostNotInKafka(post.getPostId());
                Thread.sleep(100);
            }
        }
        int notVipSubscriptionsCountSecond = 20;
        for (int j = 0; j < notVipSubscriptionsCountSecond; j++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(false);
            profileRepository.save(profile);

            additionalSubscriptions.add(subscriptionId);

            for (int i = 0; i < postsPerAuthorCount; i++) {
                PostResponse post = postService.savePost(createPostRequest(subscriptionId));
                assertNotNull(post);
                assertNull(redisService.getVipPost(subscriptionId, post.getPostId()));
                assertPostInKafka(post.getPostId());
                Thread.sleep(100);
            }
        }

        for (UUID additionalSubscription : additionalSubscriptions) {
            subscriptionsService.subscribe(new SubscriptionRequest(userId, additionalSubscription));
            subscriptions.add(additionalSubscription);
            if (!profileRepository.isVip(additionalSubscription)) {
                assertSubscriptionInKafka(userId);
                Thread.sleep(100);
                boolean found = false;
                for (int bucket = 0; bucket < 10; bucket++) {
                    if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getAuthorId().equals(additionalSubscription))) {
                        found = true;
                    }
                }
                assertTrue(found);
            }
        }
        assertEquals(vipSubscriptionsCountFirst + notVipSubscriptionsCountFirst + vipSubscriptionsCountSecond + notVipSubscriptionsCountSecond, subscriptions.size());

        Thread.sleep(1000);

        int feedLimit = 5;
        Instant lastSeenTime = null;
        Set<UUID> seenPosts = new HashSet<>();
        int expectedPostsCount = postsPerAuthorCount * subscriptions.size();
        int feedParts = (int) Math.ceil((double) (postsPerAuthorCount * subscriptions.size()) / feedLimit);
        for (int i = 0; i < feedParts; i++) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
            assertNotNull(feed);

            for (PostResponse post : feed) {
                assertNotNull(post);
                assertTrue(seenPosts.add(post.getPostId()));
                assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(userId, post.getAuthorId())));
            }

            try {
                lastSeenTime = feed.getLast().getTimeStamp();
            } catch (NoSuchElementException e) {
                fail("Потеряно " + (expectedPostsCount - seenPosts.size()) + " постов.");
            }
        }
        List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
        assertTrue(feed.isEmpty());

        assertEquals(expectedPostsCount, seenPosts.size());
    }

    @Test
    void getUserFeedAfterRemoveSubscription() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscriptions = new ArrayList<>();
        int vipSubscriptionsCount = 10;
        for (int i = 0; i < vipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(true);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
        }
        int notVipSubscriptionsCount = 20;
        for (int i = 0; i < notVipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(false);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
            assertSubscriptionInKafka(userId);
        }
        assertEquals(vipSubscriptionsCount + notVipSubscriptionsCount, subscriptions.size());

        int postsPerAuthorCount = 3;
        for (int i = 0; i < postsPerAuthorCount; i++) {
            for (UUID subscription : subscriptions) {
                PostResponse post = postService.savePost(createPostRequest(subscription));
                if (profileRepository.isVip(subscription)) {
                    assertNotNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostNotInKafka(post.getPostId());
                } else {
                    assertNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostInKafka(post.getPostId());
                    Thread.sleep(100);
                    boolean found = false;
                    for (int bucket = 0; bucket < 10; bucket++) {
                        if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getKey().getPostId().equals(post.getPostId()))) {
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
                Thread.sleep(100);
            }
        }

        List<UUID> unsubscriptions = new Random().ints(0, subscriptions.size())
                .distinct()
                .limit(10)
                .mapToObj(subscriptions::get)
                .toList();

        for (UUID subscription : unsubscriptions) {
            boolean wasVip = profileRepository.isVip(subscription);
            subscriptionsService.unsubscribe(new SubscriptionRequest(userId, subscription));
            subscriptions.remove(subscription);
            if (!wasVip) {
                assertLostSubscriptionInKafka(userId);
                Thread.sleep(100);
                boolean found = false;
                for (int bucket = 0; bucket < 10; bucket++) {
                    if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getAuthorId().equals(subscription))) {
                        found = true;
                    }
                }
                assertFalse(found);
            }
        }

        Thread.sleep(1000);

        int feedLimit = 5;
        Instant lastSeenTime = null;
        Set<UUID> seenPosts = new HashSet<>();
        int expectedPostsCount = postsPerAuthorCount * subscriptions.size();
        int feedParts = (int) Math.ceil((double) (postsPerAuthorCount * subscriptions.size()) / feedLimit);
        for (int i = 0; i < feedParts; i++) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
            assertNotNull(feed);

            for (PostResponse post : feed) {
                assertNotNull(post);
                assertTrue(seenPosts.add(post.getPostId()));
                assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(userId, post.getAuthorId())));
            }

            try {
                lastSeenTime = feed.getLast().getTimeStamp();
            } catch (NoSuchElementException e) {
                fail("Потеряно " + (expectedPostsCount - seenPosts.size()) + " постов.");
            }
        }
        List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
        assertTrue(feed.isEmpty());

        assertEquals(expectedPostsCount, seenPosts.size());
    }

    @Test
    void getSimpleUserFeedWithOtherLimit() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        Profile userProfile = createProfile(userId);
        profileRepository.save(userProfile);

        List<UUID> subscriptions = new ArrayList<>();
        int vipSubscriptionsCount = 10;
        for (int i = 0; i < vipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(true);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
        }
        int notVipSubscriptionsCount = 20;
        for (int i = 0; i < notVipSubscriptionsCount; i++) {
            UUID subscriptionId = UUID.randomUUID();
            Profile profile = createProfile(subscriptionId);
            profile.setIsVip(false);
            profileRepository.save(profile);

            subscriptionsService.subscribe(new SubscriptionRequest(userId, subscriptionId));
            subscriptions.add(subscriptionId);
            assertSubscriptionInKafka(userId);
        }
        assertEquals(vipSubscriptionsCount + notVipSubscriptionsCount, subscriptions.size());

        Thread.sleep(1000);

        int postsPerAuthorCount = 3;
        for (int i = 0; i < postsPerAuthorCount; i++) {
            for (UUID subscription : subscriptions) {
                PostResponse post = postService.savePost(createPostRequest(subscription));
                assertNotNull(post);
                if (profileRepository.isVip(subscription)) {
                    assertNotNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostNotInKafka(post.getPostId());
                } else {
                    assertNull(redisService.getVipPost(subscription, post.getPostId()));
                    assertPostInKafka(post.getPostId());
                    Thread.sleep(100);
                    boolean found = false;
                    for (int bucket = 0; bucket < 10; bucket++) {
                        if (userFeedRepository.findAllByUserIdAndBucket(userId, bucket).stream().anyMatch(f -> f.getKey().getPostId().equals(post.getPostId()))) {
                            found = true;
                        }
                    }
                    assertTrue(found);
                }
                Thread.sleep(100);
            }
        }

        Thread.sleep(1000);

        int feedLimit = 17;
        Instant lastSeenTime = null;
        Set<UUID> seenPosts = new HashSet<>();
        int expectedPostsCount = postsPerAuthorCount * subscriptions.size();
        int feedParts = (int) Math.ceil((double) (postsPerAuthorCount * subscriptions.size()) / feedLimit);
        for (int i = 0; i < feedParts; i++) {
            List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
            assertNotNull(feed);

            if (i == feedParts - 1) {
                assertEquals(expectedPostsCount - (feedParts - 1) * feedLimit, feed.size());
            }

            for (PostResponse post : feed) {
                assertNotNull(post);
                assertTrue(seenPosts.add(post.getPostId()));
                assertTrue(subscriptionsService.hasUserSubscribed(new SubscriptionRequest(userId, post.getAuthorId())));
            }

            try {
                lastSeenTime = feed.getLast().getTimeStamp();
            } catch (NoSuchElementException e) {
                fail("Потеряно " + (expectedPostsCount - seenPosts.size()) + " постов.");
            }
        }
        List<PostResponse> feed = feedService.getSubscriptionsFeed(userId, feedLimit, lastSeenTime);
        assertTrue(feed.isEmpty());

        assertEquals(postsPerAuthorCount * subscriptions.size(), seenPosts.size());
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
}