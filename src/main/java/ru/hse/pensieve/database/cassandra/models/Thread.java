package ru.hse.pensieve.database.cassandra.models;

import java.time.Instant;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Setter
@Table("threads")
public class Thread {

    @PrimaryKey
    private ThreadKey key;

    private String title;
    private Instant timeStamp;
}
