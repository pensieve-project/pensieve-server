package ru.hse.pensieve.database.cassandra.models;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Setter
@Table("themes")
public class Theme {

    @PrimaryKey
    private UUID themeId;

    private UUID authorId;
    private String title;
    private Instant timeStamp;
}
