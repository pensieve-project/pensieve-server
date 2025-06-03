package ru.hse.pensieve.subscriptions.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.posts.models.PostMapper;
import ru.hse.pensieve.subscriptions.kafka.SubscriptionEventProducer;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionsServiceImpl implements SubscriptionsService {

    @Autowired
    private SubscriptionsBySubscriberRepository subscriptionsBySubscriberRepository;

    @Autowired
    private SubscribersByTargetRepository subscribersByTargetRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SubscriptionEventProducer subscriptionEventProducer;

    @Autowired
    private FeedService feedService;

    int vipBound = 1000;

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

        Profile subscriberProfile = profileRepository.findByAuthorId(subscriberId);
        Profile targetProfile = profileRepository.findByAuthorId(targetId);

        subscriberProfile.setSubscriptionsCount(subscriberProfile.getSubscriptionsCount() + 1);
        targetProfile.setSubscribersCount(targetProfile.getSubscribersCount() + 1);

        Boolean wasVip = targetProfile.getIsVip();
        Boolean becomeVip = updateVipStatus(targetProfile);

        if (!becomeVip) {
            subscriptionEventProducer.sendSubscribed(request);
        } else if (!wasVip) {
            subscriptionEventProducer.sendBecameVip(targetId);
            feedService.cacheVipPosts(targetId);
        }

        profileRepository.save(subscriberProfile);
        profileRepository.save(targetProfile);

        Instant timeStamp = Instant.now();
        subscriptionsBySubscriberRepository.save(new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(subscriberId, targetId), timeStamp));
        subscribersByTargetRepository.save(new SubscribersByTarget(new SubscribersByTargetKey(targetId, subscriberId), timeStamp));
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

        Boolean wasVip = targetProfile.getIsVip();
        Boolean becomeVip = updateVipStatus(targetProfile);

        if (!wasVip) {
            subscriptionEventProducer.sendUnsubscribed(request);
        } else if (!becomeVip) {
            subscriptionEventProducer.sendStopVip(targetId);
            feedService.removeAllVipPostsByAuthor(targetId);
        }

        profileRepository.save(subscriberProfile);
        profileRepository.save(targetProfile);
    }

    public Boolean hasUserSubscribed(SubscriptionRequest request) {
        UUID subscriberId = request.getSubscriberId();
        UUID targetId = request.getTargetId();
        return subscriptionsBySubscriberRepository.findById(new SubscriptionsBySubscriberKey(subscriberId, targetId)).isPresent();
    }

    private Boolean updateVipStatus(Profile profile) {
        boolean isVip = profile.getSubscribersCount() > vipBound;
        profile.setIsVip(isVip);
        return isVip;
    }
}
