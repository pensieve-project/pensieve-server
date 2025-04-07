package ru.hse.pensieve.themes.models;

import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsThemeDocument;

public class ThemeMapper {
    public static ThemeResponse fromTheme(Theme theme) {
        return new ThemeResponse(
                theme.getKey().getThemeId(),
                theme.getKey().getAuthorId(),
                theme.getTitle(),
                theme.getTimeStamp()
        );
    }

    public static ThemeResponse fromEsTheme(EsThemeDocument theme) {
        return new ThemeResponse(
                theme.getThemeId(),
                theme.getAuthorId(),
                theme.getTitle(),
                theme.getTimeStamp()
        );
    }
}
