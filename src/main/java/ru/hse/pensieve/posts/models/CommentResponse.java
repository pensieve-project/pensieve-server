package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private UUID postId;
    private UUID commentId;
    private UUID authorId;
    private String text;
}
