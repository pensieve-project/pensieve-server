package ru.hse.pensieve.themes.models;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ThemeRequest {
    private UUID authorId;
    private String title;
}
