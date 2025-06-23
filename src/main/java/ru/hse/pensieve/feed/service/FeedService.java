package ru.hse.pensieve.feed.service;

import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedService {

    List<PostResponse> getSubscriptionsFeed(UUID userId, Integer limit, Instant lastSeenTime);

    void cacheVipPosts(UUID targetId);

    void removeAllVipPostsByAuthor(UUID targetId);

    List<PostResponse> getPopularFeed(Integer limit);

    List<ThemeResponse> getPopularThemes(Integer limit);
}
