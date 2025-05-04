package ru.hse.pensieve.subscriptions.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.hse.pensieve.feed.service.FeedDistributionService;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.util.UUID;

@Component
public class SubscriptionEventConsumer {

    @Autowired
    private FeedDistributionService feedDistributionService;

    @KafkaListener(topics = "user-subscribed", groupId = "feed-service")
    public void handleSubscribe(SubscriptionRequest event) {
        feedDistributionService.backfillFeedAsync(event);
    }

    @KafkaListener(topics = "user-unsubscribed", groupId = "feed-service")
    public void handleUnsubscribe(SubscriptionRequest event) {
        feedDistributionService.removePostsByAuthorAsync(event);
    }

    @KafkaListener(topics = "user-became-vip", groupId = "feed-service")
    public void handleBecameVip(UUID targetId) {
        feedDistributionService.removePostsFromFeeds(targetId);
    }

    @KafkaListener(topics = "user-stop-vip", groupId = "feed-service")
    public void handleStopVip(UUID targetId) {
        feedDistributionService.addPostsToFeeds(targetId);
    }
}
