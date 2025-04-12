package ru.hse.pensieve.profiles.models;

import org.springframework.web.multipart.MultipartFile;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProfileRequest {
    private UUID authorId;
    private MultipartFile avatar;
    private String description;
}
