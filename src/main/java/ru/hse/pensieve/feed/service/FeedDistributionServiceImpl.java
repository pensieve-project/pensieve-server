package ru.hse.pensieve.feed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.config.BucketConfig;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.util.List;

@Service
public class FeedDistributionServiceImpl implements FeedDistributionService  {

    @Autowired
    private SubscribersByTargetRepository subscribersByTargetRepository;

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private PostByAuthorRepository postByAuthorRepository;

    @Async
    public void distributeAsync(Post post) {
        List<SubscribersByTarget> followers = subscribersByTargetRepository.findByKeyTargetId(post.getKey().getAuthorId());

        for (SubscribersByTarget follower : followers) {
            userFeedRepository.save(PostMapper.feedFromPost(post, follower.getKey().getSubscriberId(), BucketConfig.getBucket(post.getKey().getPostId())));
        }
    }

    @Async
    public void backfillFeedAsync(SubscriptionRequest subscription) {
        List<UserFeed> recentPosts = postByAuthorRepository.findRecentByAuthor(subscription.getTargetId(), 10)
                .stream()
                .map(p -> PostMapper.feedFromPostByAuthor(p, subscription.getSubscriberId(), BucketConfig.getBucket(p.getKey().getPostId())))
                .toList();
        userFeedRepository.saveAll(recentPosts);
    }

    @Async
    public void removePostsByAuthorAsync(SubscriptionRequest subscription) {
        userFeedRepository.removePostsByAuthorFromFeed(subscription.getSubscriberId(), subscription.getTargetId());
    }
}

