package ru.hse.pensieve.feed.service;

import org.springframework.scheduling.annotation.Async;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface FeedDistributionService {

    @Async
    void distributeAsync(Post post);

    @Async
    void backfillFeedAsync(SubscriptionRequest subscription);

    @Async
    void removePostsByAuthorAsync(SubscriptionRequest subscription);

    @Async
    void removePostsFromFeeds(UUID targetId);

    @Async
    void addPostsToFeeds(UUID targetId);
}

