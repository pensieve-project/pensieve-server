package ru.hse.pensieve.posts.models;

import lombok.*;
import ru.hse.pensieve.database.cassandra.models.Point;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private UUID themeId;
    private UUID authorId;
    private UUID postId;
    private Point location;
    private Set<UUID> coAuthors;
    private byte[] photo;
    private String text;
    private Instant timeStamp;
    private int likesCount;
    private int commentsCount;
}
