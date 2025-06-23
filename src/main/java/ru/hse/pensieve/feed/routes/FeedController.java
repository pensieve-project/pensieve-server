package ru.hse.pensieve.feed.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.posts.models.*;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @GetMapping("/subscriptions")
    public ResponseEntity<List<PostResponse>> getSubscriptionsFeed(@RequestParam UUID userId, @RequestParam Integer limit, @RequestParam Instant lastSeenTime) {
        return ResponseEntity.ok(feedService.getSubscriptionsFeed(userId, limit, lastSeenTime));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<PostResponse>> getPopularFeed(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(feedService.getPopularFeed(limit));
    }

    @GetMapping("/popular-themes")
    public ResponseEntity<List<ThemeResponse>> getPopularThemes(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(feedService.getPopularThemes(limit));
    }
}
