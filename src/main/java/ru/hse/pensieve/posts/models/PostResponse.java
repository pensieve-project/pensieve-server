package ru.hse.pensieve.posts.models;

import java.time.Instant;
import java.util.UUID;
import lombok.*;
import ru.hse.pensieve.database.cassandra.models.Post;
import ru.hse.pensieve.database.cassandra.models.PostByAuthor;

@Getter
@Setter
@AllArgsConstructor
public class PostResponse {
    private UUID themeId;
    private UUID authorId;
    private UUID postId;
    private String text;
    private Instant timeStamp;
    private int likesCount;

    public PostResponse(Post post) {
        this.themeId = post.getKey().getThemeId();
        this.authorId = post.getKey().getAuthorId();
        this.postId = post.getKey().getPostId();
        this.text = post.getText();
        this.timeStamp = post.getTimeStamp();
        this.likesCount = post.getLikesCount();
    }

    public PostResponse(PostByAuthor post) {
        this.themeId = post.getKey().getThemeId();
        this.authorId = post.getKey().getAuthorId();
        this.postId = post.getKey().getPostId();
        this.text = post.getText();
        this.timeStamp = post.getTimeStamp();
        this.likesCount = post.getLikesCount();
    }
}
