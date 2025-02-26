package ru.hse.pensieve.database.cassandra.models;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Table("posts")
public class Post {

    @PrimaryKey
    private PostKey key;

    private UUID postId;
    private String text;
    private Instant timeStamp;
    // image
    // geo
    private int likesCount;

    public Post(PostByAuthor postByAuthor) {
        this.key = new PostKey(postByAuthor.getKey().getThreadId(), postByAuthor.getKey().getAuthorId());
        this.postId = postByAuthor.getPostId();
        this.text = postByAuthor.getText();
        this.timeStamp = postByAuthor.getTimeStamp();
        this.likesCount = postByAuthor.getLikesCount();
    }
}
