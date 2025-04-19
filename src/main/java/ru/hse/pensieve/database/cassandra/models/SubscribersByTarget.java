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
@Table("subscribers_by_target")
public class SubscribersByTarget {

    @PrimaryKey
    private SubscribersByTargetKey key;

    private Instant timeStamp;
}