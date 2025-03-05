package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private UUID themeId;
    private UUID authorId;
    private UUID postId;
    private byte[] photo;
    private String text;
    private Instant timeStamp;
    private int likesCount;
}
