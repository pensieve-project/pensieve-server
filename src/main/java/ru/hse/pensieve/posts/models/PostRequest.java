package ru.hse.pensieve.posts.models;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class PostRequest {
    private String text;
    private MultipartFile photo;
    private UUID authorId;
    private UUID themeId;
}
