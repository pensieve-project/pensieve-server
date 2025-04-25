package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@Table("subscriptions_by_subscriber")
public class SubscriptionsBySubscriber {

    @PrimaryKey
    private SubscriptionsBySubscriberKey key;

    private Instant timeStamp;
}
