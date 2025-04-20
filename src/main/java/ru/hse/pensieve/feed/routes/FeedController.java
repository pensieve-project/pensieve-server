package ru.hse.pensieve.feed.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.feed.models.SubscriptionsFeedRequest;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.posts.models.*;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping("/subscriptions")
    public ResponseEntity<List<PostResponse>> getSubscriptionsFeed(@RequestBody SubscriptionsFeedRequest request) {
        return ResponseEntity.ok(feedService.getSubscriptionsFeed(request));
    }
}
