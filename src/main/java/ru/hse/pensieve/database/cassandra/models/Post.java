package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
@Table("posts")
public class Post {

    @PrimaryKey
    private PostKey key;

    private ByteBuffer photo;
    private String text;
    private Instant timeStamp;
    private Point location;
    // friends
    private int likesCount;
    private int commentsCount;
}
