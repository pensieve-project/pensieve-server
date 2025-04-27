package ru.hse.pensieve.posts.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.feed.service.FeedDistributionService;

@Component
public class PostEventConsumer {

    @Autowired
    private FeedDistributionService feedDistributionService;

    @KafkaListener(topics = "post-created", groupId = "post-consumers")
    public void handlePostCreated(Post event) {
        feedDistributionService.distributeAsync(event);
    }
}
