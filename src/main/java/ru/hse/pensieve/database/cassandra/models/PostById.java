package ru.hse.pensieve.database.cassandra.models;

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
@Getter
@Setter
@Table("posts_by_id")
public class PostById {

    @PrimaryKey
    private PostByIdKey key;

    private ByteBuffer photo;
    private String text;
    private Point location;
    private Set<UUID> coAuthors;
    private int likesCount;
    private int commentsCount;
}