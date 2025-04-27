package ru.hse.pensieve.feed.service;

import org.springframework.scheduling.annotation.Async;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

public interface FeedDistributionService {

    @Async
    void distributeAsync(Post post);

    @Async
    void backfillFeedAsync(SubscriptionRequest subscription);

    @Async
    void removePostsByAuthorAsync(SubscriptionRequest subscription);
}

