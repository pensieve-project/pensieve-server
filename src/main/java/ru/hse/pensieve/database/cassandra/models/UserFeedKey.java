package ru.hse.pensieve.database.cassandra.models;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Setter
@PrimaryKeyClass
public class UserFeedKey implements Serializable {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private final UUID userId;

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private final Integer bucket;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private final Instant timeStamp;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private final UUID postId;
}
