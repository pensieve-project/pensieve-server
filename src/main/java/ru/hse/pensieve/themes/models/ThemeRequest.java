package ru.hse.pensieve.themes.models;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class ThemeRequest {
    private UUID authorId;
    private String title;
}
