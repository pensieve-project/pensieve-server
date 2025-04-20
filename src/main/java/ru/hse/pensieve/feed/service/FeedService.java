package ru.hse.pensieve.feed.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.database.redis.service.RedisService;
import ru.hse.pensieve.feed.models.SubscriptionsFeedRequest;
import ru.hse.pensieve.posts.models.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FeedService {

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private VipPostRepository vipPostRepository;

    @Autowired
    private SubscriptionsBySubscriberRepository subscriptionsBySubscriberRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private RedisService redisService;

    private List<UUID> findVipSubscriptions(UUID subscriberId) {
        List<SubscriptionsBySubscriber> subscriptions = subscriptionsBySubscriberRepository.findByKeySubscriberId(subscriberId);

        return subscriptions.stream()
                .map(s -> s.getKey().getTargetId())
                .filter(profileRepository::isVip)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getSubscriptionsFeed(SubscriptionsFeedRequest request) {
        UUID userId = request.getUserId();
        int limit = request.getLimit();
        Instant lastSeen = request.getLastSeenTime() != null ? request.getLastSeenTime() : Instant.now();

        List<Post> regularPosts = getRegularPosts(userId, limit, lastSeen);

        List<UUID> vipAuthors = findVipSubscriptions(request.getUserId());
        List<Post> vipPosts = getVipPosts(vipAuthors, limit, lastSeen);

        return Stream.concat(regularPosts.stream(), vipPosts.stream())
                .sorted(Comparator.comparing(Post::getTimeStamp).reversed())
                .limit(limit)
                .map(PostMapper::fromPost)
                .collect(Collectors.toList());
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
                        return vipPostRepository.findLatestByAuthor(authorId, lastSeen, perAuthorLimit)
                                .stream()
                                .map(PostMapper::postFromVip);
                    }

                    return cachedPosts.stream();
                })
                .collect(Collectors.toList());
    }
}
