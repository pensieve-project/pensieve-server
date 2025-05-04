package ru.hse.pensieve.themes.service;

import ru.hse.pensieve.themes.models.LikeRequest;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.util.List;
import java.util.UUID;

public interface ThemeService {

    ThemeResponse createTheme(ThemeRequest request);
    
    List<ThemeResponse> getLikedThemes(UUID authorId);
    
    List<ThemeResponse> getAllThemes();

    List<ThemeResponse> getLikedThemes(UUID authorId);

    String getThemeTitle(UUID themeId);

    Boolean hasUserLikedTheme(LikeRequest request);

    void likeTheme(LikeRequest request);

    void unlikeTheme(LikeRequest request);
}
