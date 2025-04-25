package ru.hse.pensieve.database.cassandra.models;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

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
public class VipPostKey implements Serializable {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private final UUID authorId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private final Instant timeStamp;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private final UUID postId;
}
