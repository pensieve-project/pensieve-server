package ru.hse.pensieve.subscriptions.service;

import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.util.List;
import java.util.UUID;

public interface SubscriptionsService {

    int vipBound = 1000;

    List<UUID> getSubscriptions(UUID subscriberId);

    Integer getSubscriptionsCount(UUID subscriberId);

    List<UUID> getSubscribers(UUID targetId);

    Integer getSubscribersCount(UUID targetId);

    void subscribe(SubscriptionRequest request);

    void unsubscribe(SubscriptionRequest request);

    Boolean hasUserSubscribed(SubscriptionRequest request);
}
