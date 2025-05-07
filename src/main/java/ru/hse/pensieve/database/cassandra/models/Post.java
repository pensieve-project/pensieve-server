package ru.hse.pensieve.database.cassandra.models;

import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("posts")
public class Post {

    @PrimaryKey
    private PostKey key;

    private ByteBuffer photo;
    private String text;
    private Point location;
    private Set<UUID> coAuthors;
    private UUID albumId;
    private int likesCount;
    private int commentsCount;
}
