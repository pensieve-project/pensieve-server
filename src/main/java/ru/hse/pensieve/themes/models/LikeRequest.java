package ru.hse.pensieve.themes.models;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LikeRequest {
    private UUID authorId;
    private UUID themeId;
}
