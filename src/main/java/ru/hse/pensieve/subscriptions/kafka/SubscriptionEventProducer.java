package ru.hse.pensieve.subscriptions.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.util.UUID;

@Service
public class SubscriptionEventProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendSubscribed(SubscriptionRequest event) {
        kafkaTemplate.send("user-subscribed", event.getSubscriberId().toString(), event);
    }

    public void sendUnsubscribed(SubscriptionRequest event) {
        kafkaTemplate.send("user-unsubscribed", event.getSubscriberId().toString(), event);
    }

    public void sendBecameVip(UUID targetId) {
        kafkaTemplate.send("user-became-vip", targetId.toString(), targetId);
    }

    public void sendStopVip(UUID targetId) {
        kafkaTemplate.send("user-stop-vip", targetId.toString(), targetId);
    }
}
