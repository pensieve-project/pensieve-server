package ru.hse.pensieve.posts.models;

import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class PostRequest {
    private String text;
    private UUID authorId;
    private UUID threadId;
}
