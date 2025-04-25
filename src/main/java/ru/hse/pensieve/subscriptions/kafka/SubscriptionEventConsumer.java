package ru.hse.pensieve.subscriptions.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.hse.pensieve.feed.service.FeedDistributionService;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

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
}
