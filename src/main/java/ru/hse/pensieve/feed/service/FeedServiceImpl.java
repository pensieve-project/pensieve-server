package ru.hse.pensieve.feed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.PostRankingService;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.posts.models.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FeedServiceImpl implements FeedService {

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private PostByAuthorRepository postByAuthorRepository;

    @Autowired
    private SubscriptionsBySubscriberRepository subscriptionsBySubscriberRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PostRankingService postRankingService;

    private List<UUID> findVipSubscriptions(UUID subscriberId) {
        List<SubscriptionsBySubscriber> subscriptions = subscriptionsBySubscriberRepository.findByKeySubscriberId(subscriberId);

        return subscriptions.stream()
                .map(s -> s.getKey().getTargetId())
                .filter(profileRepository::isVip)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getSubscriptionsFeed(UUID userId, Integer limit, Instant lastSeenTime) {
        Instant lastSeen = lastSeenTime != null ? lastSeenTime : Instant.now();

        List<Post> regularPosts = getRegularPosts(userId, limit, lastSeen);

        List<UUID> vipAuthors = findVipSubscriptions(userId);
        List<Post> vipPosts = getVipPosts(vipAuthors, limit, lastSeen);

        List<PostResponse> result = Stream.concat(regularPosts.stream(), vipPosts.stream())
                .sorted(Comparator.<Post, Instant>comparing(post -> post.getKey().getTimeStamp()).reversed())
                .limit(limit)
                .map(PostMapper::fromPost)
                .collect(Collectors.toList());

        System.out.println("Sent " + result.size() + " posts in feed");
        return result;
    }

    private List<Post> getRegularPosts(UUID userId, int limit, Instant lastSeen) {
        return userFeedRepository.findUserFeed(userId, limit, lastSeen).stream().map(PostMapper::postFromUserFeed).toList();
    }

    private List<Post> getVipPosts(List<UUID> vipAuthors, int limit, Instant lastSeen) {
        if (vipAuthors.isEmpty()) {
            return Collections.emptyList();
        }

        int perAuthorLimit = Math.max(1, limit / vipAuthors.size());

        return vipAuthors.parallelStream()
                .flatMap(authorId -> {
                    List<Post> cachedPosts = redisService.getLatestVipPosts(authorId, lastSeen, perAuthorLimit);

                    if (cachedPosts.isEmpty()) {
                        return postByAuthorRepository.findRecentByAuthor(authorId, lastSeen, perAuthorLimit)
                                .stream()
                                .map(PostMapper::postFromPostByAuthor);
                    }

                    return cachedPosts.stream();
                })
                .collect(Collectors.toList());
    }

    public void cacheVipPosts(UUID targetId) {
        List<PostByAuthor> posts = postByAuthorRepository.findRecentByAuthor(targetId, 50);
        posts.forEach(post ->
                redisService.cacheVipPost(PostMapper.postFromPostByAuthor(post))
        );
    }

    public void removeAllVipPostsByAuthor(UUID targetId) {
        redisService.deleteAllPostsByAuthor(targetId);
    }

    public List<PostResponse> getPopularFeed(Integer limit) {
        return postRankingService.getTopPosts(limit).stream()
                .map(PostMapper::fromPost)
                .toList();
    }
}
