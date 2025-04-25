package ru.hse.pensieve.subscriptions.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.subscriptions.models.*;
import ru.hse.pensieve.subscriptions.service.SubscriptionsService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionsController {

    @Autowired
    private SubscriptionsService subscriptionsService;

    @GetMapping("/subscriptions")
    public ResponseEntity<List<UUID>> getSubscriptions(@RequestParam UUID subscriberId) {
        return ResponseEntity.ok(subscriptionsService.getSubscriptions(subscriberId));
    }

    @GetMapping("/subscriptions-count")
    public ResponseEntity<Integer> getSubscriptionsCount(@RequestParam UUID subscriberId) {
        return ResponseEntity.ok(subscriptionsService.getSubscriptionsCount(subscriberId));
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<UUID>> getSubscribers(@RequestParam UUID targetId) {
        return ResponseEntity.ok(subscriptionsService.getSubscribers(targetId));
    }

    @GetMapping("/subscribers-count")
    public ResponseEntity<Integer> getSubscribersCount(@RequestParam UUID targetId) {
        return ResponseEntity.ok(subscriptionsService.getSubscribersCount(targetId));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody SubscriptionRequest request) {
        subscriptionsService.subscribe(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody SubscriptionRequest request) {
        subscriptionsService.unsubscribe(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subscribed")
    public ResponseEntity<Boolean> hasUserSubscribed(@RequestParam SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionsService.hasUserSubscribed(request));
    }
}
