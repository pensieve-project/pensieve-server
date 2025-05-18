package ru.hse.pensieve.feed;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.feed.service.FeedServiceImpl;
import ru.hse.pensieve.posts.models.PostResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTest {

    @Mock
    private UserFeedRepository userFeedRepository;

    @Mock
    private PostByAuthorRepository postByAuthorRepository;

    @Mock
    private SubscriptionsBySubscriberRepository subscriptionsBySubscriberRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private FeedServiceImpl feedService;

    @Captor
    private ArgumentCaptor<Post> postCaptor;

    private final UUID userId = UUID.randomUUID();
    private final Instant now = Instant.now();

    @Test
    void getSubscriptionsFeed_ShouldCombineAndSortPosts() {
        Post regularPost = createPost(now.minusSeconds(60), "Test");
        Post vipPost1 = createPost(now.minusSeconds(30), "Test");
        Post vipPost2 = createPost(now.minusSeconds(15), "Test");

        UUID vipAuthor1 = UUID.randomUUID();
        UUID vipAuthor2 = UUID.randomUUID();

        when(subscriptionsBySubscriberRepository.findByKeySubscriberId(userId))
                .thenReturn(List.of(
                        new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(userId, vipAuthor1), now),
                        new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(userId, vipAuthor2), now)
                ));

        when(profileRepository.isVip(vipAuthor1)).thenReturn(true);
        when(profileRepository.isVip(vipAuthor2)).thenReturn(true);

        when(userFeedRepository.findUserFeed(userId, 10, now)).thenReturn(List.of(createUserFeed(regularPost)));

        when(redisService.getLatestVipPosts(Mockito.any(), Mockito.any(), anyInt())).thenReturn(List.of());
        when(postByAuthorRepository.findRecentByAuthor(vipAuthor1, now, 5))
                .thenReturn(List.of(createPostByAuthor(vipPost1)));
        when(postByAuthorRepository.findRecentByAuthor(vipAuthor2, now, 5))
                .thenReturn(List.of(createPostByAuthor(vipPost2)));

        List<PostResponse> result = feedService.getSubscriptionsFeed(userId, 10, now);

        assertEquals(3, result.size());
        assertTrue(result.get(0).getTimeStamp().isAfter(result.get(1).getTimeStamp()));
        assertTrue(result.get(1).getTimeStamp().isAfter(result.get(2).getTimeStamp()));
        verify(redisService, times(2)).getLatestVipPosts(Mockito.any(), Mockito.any(), anyInt());
    }

    @Test
    void getSubscriptionsFeed_ShouldUseCachedVipPosts() {
        Post cachedPost = createPost(now.minusSeconds(10), "Test");

        UUID vipAuthor = UUID.randomUUID();

        when(subscriptionsBySubscriberRepository.findByKeySubscriberId(userId))
                .thenReturn(List.of(
                        new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(userId, vipAuthor), now)
                ));

        when(profileRepository.isVip(vipAuthor)).thenReturn(true);

        when(redisService.getLatestVipPosts(vipAuthor, now, 10)).thenReturn(List.of(cachedPost));

        List<PostResponse> result = feedService.getSubscriptionsFeed(userId, 10, now);

        assertEquals(1, result.size());
        verify(postByAuthorRepository, never()).findRecentByAuthor(any(), any(), anyInt());
    }

    @Test
    void cacheVipPosts_ShouldSaveToRedis() {
        UUID authorId = UUID.randomUUID();
        PostByAuthor post1 = createPostByAuthor(createPost(now, "Content 1"));
        PostByAuthor post2 = createPostByAuthor(createPost(now.minusSeconds(10), "Content 2"));

        when(postByAuthorRepository.findRecentByAuthor(authorId, 50)).thenReturn(List.of(post1, post2));

        feedService.cacheVipPosts(authorId);

        verify(redisService, times(2)).cacheVipPost(postCaptor.capture());

        List<Post> capturedPosts = postCaptor.getAllValues();

        assertTrue(capturedPosts.stream().anyMatch(p -> p.getText().equals("Content 1")));
        assertTrue(capturedPosts.stream().anyMatch(p -> p.getText().equals("Content 2")));
    }

    @Test
    void removeAllVipPostsByAuthor_ShouldCallRedisService() {
        UUID authorId = UUID.randomUUID();

        feedService.removeAllVipPostsByAuthor(authorId);

        verify(redisService).deleteAllPostsByAuthor(authorId);
    }

    private Post createPost(Instant timestamp, String text) {
        PostKey key = new PostKey(UUID.randomUUID(), timestamp, UUID.randomUUID(), UUID.randomUUID());
        return new Post(key, null, text, null, Set.of(), UUID.randomUUID(), 0, 0);
    }

    private UserFeed createUserFeed(Post post) {
        return new UserFeed(
                new UserFeedKey(
                        userId,
                        post.getKey().getPostId().hashCode() % 10,
                        post.getKey().getTimeStamp(),
                        post.getKey().getPostId()
                ),
                post.getKey().getThemeId(),
                post.getKey().getAuthorId(),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }

    private PostByAuthor createPostByAuthor(Post post) {
        return new PostByAuthor(
                new PostByAuthorKey(
                        post.getKey().getAuthorId(),
                        post.getKey().getTimeStamp(),
                        post.getKey().getThemeId(),
                        post.getKey().getPostId()
                ),
                post.getPhoto(),
                post.getText(),
                post.getLocation(),
                post.getCoAuthors(),
                post.getAlbumId(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}
