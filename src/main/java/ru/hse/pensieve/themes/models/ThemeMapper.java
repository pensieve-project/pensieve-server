package ru.hse.pensieve.themes.models;

import ru.hse.pensieve.database.cassandra.models.Theme;

public class ThemeMapper {
    public static ThemeResponse fromTheme(Theme theme) {
        return new ThemeResponse(
                theme.getThemeId(),
                theme.getAuthorId(),
                theme.getTitle(),
                theme.getTimeStamp()
        );
    }
}
