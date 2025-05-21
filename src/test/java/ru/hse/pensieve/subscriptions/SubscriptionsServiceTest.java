package ru.hse.pensieve.subscriptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.database.cassandra.models.*;
import ru.hse.pensieve.database.cassandra.repositories.*;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.subscriptions.kafka.SubscriptionEventProducer;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;
import ru.hse.pensieve.subscriptions.service.SubscriptionsServiceImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceTest {

    @Mock
    private SubscriptionsBySubscriberRepository subscriptionsRepo;

    @Mock
    private SubscribersByTargetRepository subscribersRepo;

    @Mock
    private ProfileRepository profileRepo;

    @Mock
    private SubscriptionEventProducer eventProducer;

    @Mock
    private FeedService feedService;

    @InjectMocks
    private SubscriptionsServiceImpl subscriptionsService;

    private final UUID subscriberId = UUID.randomUUID();
    private final UUID targetId = UUID.randomUUID();
    private final SubscriptionRequest request = new SubscriptionRequest(subscriberId, targetId);

    @Test
    void subscribe_ShouldAddNewSubscription() {
        Profile subscriberProfile = createProfile(subscriberId);
        Profile targetProfile = createProfile(targetId);
        when(profileRepo.findByAuthorId(subscriberId)).thenReturn(subscriberProfile);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(targetProfile);

        when(subscriptionsRepo.findById(any())).thenReturn(Optional.empty());

        subscriptionsService.subscribe(request);

        verify(subscriptionsRepo).save(any(SubscriptionsBySubscriber.class));
        verify(subscribersRepo).save(any(SubscribersByTarget.class));
        assertEquals(1, subscriberProfile.getSubscriptionsCount());
        assertEquals(1, targetProfile.getSubscribersCount());
        verify(eventProducer).sendSubscribed(request);
    }

    @Test
    void subscribe_ShouldMakeUserVipWhenReachingThreshold() {
        Profile subscriberProfile = createProfile(subscriberId);
        Profile targetProfile = createProfile(targetId);
        targetProfile.setSubscribersCount(10000);
        when(profileRepo.findByAuthorId(subscriberId)).thenReturn(subscriberProfile);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(targetProfile);

        when(subscriptionsRepo.findById(any())).thenReturn(Optional.empty());

        subscriptionsService.subscribe(request);

        assertTrue(targetProfile.getIsVip());
        verify(eventProducer).sendBecameVip(targetId);
        verify(feedService).cacheVipPosts(targetId);
    }

    @Test
    void unsubscribe_ShouldRemoveSubscription() {
        Profile subscriberProfile = createProfile(subscriberId);
        subscriberProfile.setSubscriptionsCount(1);
        Profile targetProfile = createProfile(targetId);
        targetProfile.setSubscribersCount(1);
        when(profileRepo.findByAuthorId(subscriberId)).thenReturn(subscriberProfile);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(targetProfile);

        when(subscriptionsRepo.findById(any())).thenReturn(Optional.of(createSubscriptionsBySubscriber()));

        subscriptionsService.unsubscribe(request);

        verify(subscriptionsRepo).deleteById(any());
        verify(subscribersRepo).deleteById(any());
        assertEquals(0, subscriberProfile.getSubscriptionsCount());
        assertEquals(0, targetProfile.getSubscribersCount());
        verify(eventProducer).sendUnsubscribed(request);
    }

    @Test
    void unsubscribe_ShouldRemoveVipStatusWhenBelowThreshold() {
        Profile subscriberProfile = createProfile(subscriberId);
        Profile targetProfile = createProfile(targetId);
        targetProfile.setSubscribersCount(10001);
        targetProfile.setIsVip(true);
        when(profileRepo.findByAuthorId(subscriberId)).thenReturn(subscriberProfile);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(targetProfile);

        when(subscriptionsRepo.findById(any())).thenReturn(Optional.of(createSubscriptionsBySubscriber()));

        subscriptionsService.unsubscribe(request);

        assertFalse(targetProfile.getIsVip());
        verify(eventProducer).sendStopVip(targetId);
        verify(feedService).removeAllVipPostsByAuthor(targetId);
    }

    @Test
    void getSubscriptions_ShouldReturnListOfSubscriptions() {
        UUID target1 = UUID.randomUUID();
        UUID target2 = UUID.randomUUID();
        List<SubscriptionsBySubscriber> subscriptions = List.of(
                new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(subscriberId, target1), Instant.now()),
                new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(subscriberId, target2), Instant.now())
        );
        when(subscriptionsRepo.findByKeySubscriberId(subscriberId)).thenReturn(subscriptions);

        List<UUID> result = subscriptionsService.getSubscriptions(subscriberId);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(target1, target2)));
    }

    @Test
    void hasUserSubscribed_ShouldReturnTrueWhenSubscriptionExists() {
        when(subscriptionsRepo.findById(any())).thenReturn(Optional.of(createSubscriptionsBySubscriber()));

        boolean result = subscriptionsService.hasUserSubscribed(request);

        assertTrue(result);
    }

    @Test
    void hasUserSubscribed_ShouldReturnFalseWhenNoSubscription() {
        when(subscriptionsRepo.findById(any())).thenReturn(Optional.empty());

        boolean result = subscriptionsService.hasUserSubscribed(request);

        assertFalse(result);
    }

    @Test
    void getSubscribersCount_ShouldReturnValueFromProfile() {
        int expectedCount = 42;
        Profile profile = createProfile(targetId);
        profile.setSubscribersCount(expectedCount);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(profile);

        int result = subscriptionsService.getSubscribersCount(targetId);

        assertEquals(expectedCount, result);
    }

    @Test
    void subscribe_ShouldNotSendVipEventsWhenAlreadyVip() {
        Profile subscriberProfile = createProfile(subscriberId);
        Profile targetProfile = createProfile(targetId);
        targetProfile.setSubscribersCount(10001);
        targetProfile.setIsVip(true);
        when(profileRepo.findByAuthorId(targetId)).thenReturn(targetProfile);
        when(profileRepo.findByAuthorId(subscriberId)).thenReturn(subscriberProfile);

        when(subscriptionsRepo.findById(any())).thenReturn(Optional.empty());

        subscriptionsService.subscribe(request);

        verify(eventProducer, never()).sendBecameVip(any());
        verify(feedService, never()).cacheVipPosts(any());
    }

    private Profile createProfile(UUID authorId) {
        return new Profile(
                authorId,
                null,
                "description",
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                0,
                false
        );
    }

    private SubscriptionsBySubscriber createSubscriptionsBySubscriber() {
        return new SubscriptionsBySubscriber(new SubscriptionsBySubscriberKey(subscriberId, targetId), Instant.now());
    }
}