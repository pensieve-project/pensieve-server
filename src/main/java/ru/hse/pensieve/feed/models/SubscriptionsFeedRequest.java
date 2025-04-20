package ru.hse.pensieve.feed.models;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SubscriptionsFeedRequest {
    private UUID userId;
    private Integer limit;
    private Instant lastSeenTime;
}
