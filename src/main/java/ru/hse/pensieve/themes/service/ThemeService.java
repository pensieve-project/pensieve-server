package ru.hse.pensieve.themes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.repositories.ProfileRepository;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.themes.models.LikeRequest;
import ru.hse.pensieve.themes.models.ThemeMapper;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ThemeService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ThemeRepository themeRepository;

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = new Theme(UUID.randomUUID(), request.getAuthorId(), request.getTitle(), Instant.now());
        Theme newTheme = themeRepository.save(theme);
        return ThemeMapper.fromTheme(newTheme);
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream().map(ThemeMapper::fromTheme).toList();
    }

    public String getThemeTitle(UUID themeId) {
        Optional<Theme> theme = themeRepository.findById(themeId);
        if (theme.isEmpty()) {
            return "";
        }
        return theme.get().getTitle();
    }

    public Boolean hasUserLikedTheme(LikeRequest request) {
        return profileRepository.hasLikedTheme(request.getAuthorId(), request.getThemeId());
    }

    public void likeTheme(LikeRequest request) {
        if (hasUserLikedTheme(request)) {
            return;
        }
        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedThemesIds();
        if (likes == null) {
            likes = new ArrayList<>();
        }
        likes.add(request.getThemeId());
        profile.setLikedThemesIds(likes);
        profileRepository.save(profile);
    }

    public void unlikeTheme(LikeRequest request) {
        if (!hasUserLikedTheme(request)) {
            return;
        }
        Profile profile = profileRepository.findByAuthorId(request.getAuthorId());
        ArrayList<UUID> likes = profile.getLikedThemesIds();
        if (likes == null) {
            return;
        }
        likes.remove(request.getThemeId());
        profile.setLikedThemesIds(likes);
        profileRepository.save(profile);
    }
}
