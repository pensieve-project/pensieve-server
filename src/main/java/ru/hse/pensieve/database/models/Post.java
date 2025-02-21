package ru.hse.pensieve.database.models;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("posts")
public class Post {

    @PrimaryKey
    private UUID id;

    private String text;
    private UUID authorId;
    private UUID threadId;

    public Post(UUID id, String text, UUID authorId, UUID threadId) {
        this.id = id;
        this.text = text;
        this.authorId = authorId;
        this.threadId = threadId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public void setThreadId(UUID threadId) {
        this.threadId = threadId;
    }
}
