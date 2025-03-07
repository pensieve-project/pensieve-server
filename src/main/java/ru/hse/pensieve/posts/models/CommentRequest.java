package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CommentRequest {
    private UUID postId;
    private UUID authorId;
    private String text;
}
