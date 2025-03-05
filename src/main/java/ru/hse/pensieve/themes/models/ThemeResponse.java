package ru.hse.pensieve.themes.models;

import java.time.Instant;
import java.util.UUID;
import lombok.*;
import ru.hse.pensieve.database.cassandra.models.Theme;

@Getter
@Setter
@AllArgsConstructor
public class ThemeResponse {
    private UUID themeId;
    private UUID authorId;
    private String title;
    private Instant timeStamp;

    public ThemeResponse(Theme theme) {
        this.themeId = theme.getKey().getThemeId();
        this.authorId = theme.getKey().getAuthorId();
        this.title = theme.getTitle();
        this.timeStamp = theme.getTimeStamp();
    }
}
