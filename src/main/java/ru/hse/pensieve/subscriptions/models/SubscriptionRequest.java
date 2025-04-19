package ru.hse.pensieve.subscriptions.models;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SubscriptionRequest {
    private UUID subscriberId;
    private UUID targetId;
}
