package ru.hse.pensieve.themes.models;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ThemeResponse {
    private UUID themeId;
    private UUID authorId;
    private String title;
    private Instant timeStamp;
}
