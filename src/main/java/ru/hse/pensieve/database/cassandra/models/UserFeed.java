package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("user_feed")
public class UserFeed {

    @PrimaryKey
    private UserFeedKey key;

    private UUID themeId;
    private UUID authorId;
    private ByteBuffer photo;
    private String text;
    private Point location;
    // friends
    private int likesCount;
    private int commentsCount;
}