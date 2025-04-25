package ru.hse.pensieve.posts.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Post;

@Service
public class PostEventProducer {

    @Autowired
    private KafkaTemplate<String, Post> kafkaTemplate;

    public void sendPostCreated(Post event) {
        kafkaTemplate.send("post-created", event.getKey().getPostId().toString(), event);
    }
}
