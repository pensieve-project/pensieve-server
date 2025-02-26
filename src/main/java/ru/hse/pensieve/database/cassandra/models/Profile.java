package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("profiles")
public class Profile {

    @PrimaryKey
    private UUID authorId;

    // avatar
    private String description;
    // arrayLikedThreadIds
}
