package ru.hse.pensieve.database.cassandra.models;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@PrimaryKeyClass
public class PostKey implements Serializable {

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    private UUID themeId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
    private Instant timeStamp;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private UUID authorId;

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    private UUID postId;
}
