package ru.hse.pensieve.feed.service;

import ru.hse.pensieve.posts.models.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FeedService {

    List<PostResponse> getSubscriptionsFeed(UUID userId, Integer limit, Instant lastSeenTime);

}
