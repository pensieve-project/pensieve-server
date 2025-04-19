package ru.hse.pensieve.subscriptions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionsService {

    @Autowired
    private SubscriptionsBySubscriberRepository subscriptionsBySubscriberRepository;

    @Autowired
    private SubscribersByTargetRepository subscribersByTargetRepository;

    @Autowired
    private ProfileRepository profileRepository;

    public List<UUID> getSubscriptions(UUID subscriberId) {
        return subscriptionsBySubscriberRepository.findByKeySubscriberId(subscriberId).stream().map(s -> s.getKey().getTargetId()).toList();
    }

    public Integer getSubscriptionsCount(UUID subscriberId) {
        return profileRepository.findByAuthorId(subscriberId).getSubscriptionsCount();
    }

    public List<UUID> getSubscribers(UUID targetId) {
        return subscribersByTargetRepository.findByKeyTargetId(targetId).stream().map(s -> s.getKey().getSubscriberId()).toList();
    }

    public Integer getSubscribersCount(UUID targetId) {
        return profileRepository.findByAuthorId(targetId).getSubscribersCount();
    }

    public void subscribe(SubscriptionRequest request) {
        UUID subscriberId = request.getSubscriberId();
        UUID targetId = request.getTargetId();

        if (hasUserSubscribed(request)) {
            return;
        }

        Instant timeStamp = Instant.now();
        subscriptionsBySubscriberRepository.save(new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(subscriberId, targetId), timeStamp));
        subscribersByTargetRepository.save(new SubscribersByTarget(new SubscribersByTargetKey(targetId, subscriberId), timeStamp));

        Profile subscriberProfile = profileRepository.findByAuthorId(subscriberId);
        Profile targetProfile = profileRepository.findByAuthorId(targetId);

        subscriberProfile.setSubscriptionsCount(subscriberProfile.getSubscriptionsCount() + 1);
        targetProfile.setSubscribersCount(targetProfile.getSubscribersCount() + 1);

        profileRepository.save(subscriberProfile);
        profileRepository.save(targetProfile);
    }

    public void unsubscribe(SubscriptionRequest request) {
        UUID subscriberId = request.getSubscriberId();
        UUID targetId = request.getTargetId();

        if (!hasUserSubscribed(request)) {
            return;
        }

        subscriptionsBySubscriberRepository.deleteById(new SubscriptionsBySubscriberKey(subscriberId, targetId));
        subscribersByTargetRepository.deleteById(new SubscribersByTargetKey(targetId, subscriberId));

        Profile subscriberProfile = profileRepository.findByAuthorId(subscriberId);
        Profile targetProfile = profileRepository.findByAuthorId(targetId);

        subscriberProfile.setSubscriptionsCount(subscriberProfile.getSubscriptionsCount() - 1);
        targetProfile.setSubscribersCount(targetProfile.getSubscribersCount() - 1);

        profileRepository.save(subscriberProfile);
        profileRepository.save(targetProfile);
    }

    public Boolean hasUserSubscribed(SubscriptionRequest request) {
        UUID subscriberId = request.getSubscriberId();
        UUID targetId = request.getTargetId();
        return subscriptionsBySubscriberRepository.findById(new SubscriptionsBySubscriberKey(subscriberId, targetId)).isPresent();
    }
}
