package ru.hse.pensieve.subscriptions.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

@Service
public class SubscriptionEventProducer {

    @Autowired
    private KafkaTemplate<String, SubscriptionRequest> kafkaTemplate;

    public void sendSubscribed(SubscriptionRequest event) {
        kafkaTemplate.send("user-subscribed", event.getSubscriberId().toString(), event);
    }

    public void sendUnsubscribed(SubscriptionRequest event) {
        kafkaTemplate.send("user-unsubscribed", event.getSubscriberId().toString(), event);
    }
}
