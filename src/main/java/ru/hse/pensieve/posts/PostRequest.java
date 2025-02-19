package ru.hse.pensieve.posts;

import java.util.UUID;

public class PostRequest {
    private String text;
    private UUID authorId;
    private UUID threadId;

    public String getText() {
        return text;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getThreadId() {
        return threadId;
    }
}
