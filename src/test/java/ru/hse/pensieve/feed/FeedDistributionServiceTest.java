package ru.hse.pensieve.feed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import ru.hse.pensieve.database.cassandra.config.BucketConfig;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.feed.service.FeedDistributionServiceImpl;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class FeedDistributionServiceTest {

    @Mock
    private SubscribersByTargetRepository subscribersByTargetRepository;

    @Mock
    private UserFeedRepository userFeedRepository;

    @Mock
    private PostByAuthorRepository postByAuthorRepository;

    @Mock
    private BucketConfig bucketConfig;

    @Mock
    @Qualifier("feedTaskExecutor")
    private Executor taskExecutor;

    @InjectMocks
    private FeedDistributionServiceImpl feedDistributionService;

    @Captor
    private ArgumentCaptor<UserFeed> userFeedCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidCaptor;

    private final UUID authorId = UUID.randomUUID();
    private final UUID subscriberId = UUID.randomUUID();
    private final UUID postId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(BucketConfig.class, "bucketCount", 10);
    }

    @Test
    void distributeAsync_ShouldSavePostToAllSubscribersFeeds() {
        Post post = createTestPost(authorId);
        List<SubscribersByTarget> subscribers = List.of(
                new SubscribersByTarget(new SubscribersByTargetKey(authorId, subscriberId), Instant.now())
        );

        when(subscribersByTargetRepository.findByKeyTargetId(authorId)).thenReturn(subscribers);

        feedDistributionService.distributeAsync(post);

        verify(userFeedRepository, timeout(1000)).save(userFeedCaptor.capture());
        UserFeed savedFeed = userFeedCaptor.getValue();

        assertEquals(subscriberId, savedFeed.getKey().getUserId());
        assertEquals(postId, savedFeed.getKey().getPostId());
        assertEquals(BucketConfig.getBucket(postId), savedFeed.getKey().getBucket());
    }

    @Test
    void backfillFeedAsync_ShouldAddRecentPostsToSubscriberFeed() {
        SubscriptionRequest request = new SubscriptionRequest(subscriberId, authorId);
        PostByAuthor post = createTestPostByAuthor(authorId);

        when(postByAuthorRepository.findRecentByAuthor(authorId, 10)).thenReturn(List.of(post));

        feedDistributionService.backfillFeedAsync(request);

        verify(userFeedRepository, timeout(1000)).saveAll(any());
        verify(postByAuthorRepository).findRecentByAuthor(authorId, 10);
    }

    @Test
    void removePostsByAuthorAsync_ShouldRemoveAuthorsPostsFromFeed() {
        SubscriptionRequest request = new SubscriptionRequest(subscriberId, authorId);

        feedDistributionService.removePostsByAuthorAsync(request);

        verify(userFeedRepository, timeout(1000)).removePostsByAuthorFromFeed(subscriberId, authorId);
    }

    @Test
    void removePostsFromFeeds_ShouldRemoveFromAllSubscribers() {
        UUID targetId = UUID.randomUUID();
        List<SubscribersByTarget> subscribers = List.of(
                new SubscribersByTarget(new SubscribersByTargetKey(targetId, subscriberId), Instant.now()),
                new SubscribersByTarget(new SubscribersByTargetKey(targetId, UUID.randomUUID()), Instant.now())
        );

        when(subscribersByTargetRepository.findByKeyTargetId(targetId)).thenReturn(subscribers);
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        feedDistributionService.removePostsFromFeeds(targetId);

        verify(userFeedRepository, times(2)).removePostsByAuthorFromFeed(any(UUID.class), eq(targetId));
    }

    @Test
    void addPostsToFeeds_ShouldAddPostsToAllSubscribers() {
        UUID targetId = UUID.randomUUID();
        PostByAuthor post = createTestPostByAuthor(targetId);
        List<SubscribersByTarget> subscribers = List.of(
                new SubscribersByTarget(new SubscribersByTargetKey(targetId, subscriberId), Instant.now()),
                new SubscribersByTarget(new SubscribersByTargetKey(targetId, UUID.randomUUID()), Instant.now())
        );

        when(subscribersByTargetRepository.findByKeyTargetId(targetId)).thenReturn(subscribers);
        when(postByAuthorRepository.findRecentByAuthor(targetId, 10)).thenReturn(List.of(post));
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        feedDistributionService.addPostsToFeeds(targetId);

        verify(userFeedRepository, times(2)).saveAll(anyList());
    }

    private Post createTestPost(UUID authorId) {
        PostKey key = new PostKey(
                UUID.randomUUID(),
                Instant.now(),
                authorId,
                postId
        );
        return new Post(key, null, "Test", null, null, null, 0, 0);
    }

    private PostByAuthor createTestPostByAuthor(UUID authorId) {
        PostByAuthorKey key = new PostByAuthorKey(
                authorId,
                Instant.now(),
                UUID.randomUUID(),
                postId
        );
        return new PostByAuthor(key, null, "Test", null, null, null, 0, 0);
    }
}