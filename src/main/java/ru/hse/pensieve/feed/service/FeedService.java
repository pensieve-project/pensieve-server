package ru.hse.pensieve.feed.service;

import ru.hse.pensieve.database.cassandra.models.PostByAuthor;
import ru.hse.pensieve.posts.models.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedService {

    List<PostResponse> getSubscriptionsFeed(UUID userId, Integer limit, Instant lastSeenTime);

    void cacheVipPosts(UUID targetId);

    void removeAllVipPostsByAuthor(UUID targetId);

    List<PostResponse> getPopularFeed(Integer limit);
}
