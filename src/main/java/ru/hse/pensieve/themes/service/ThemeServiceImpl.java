package ru.hse.pensieve.themes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.repositories.ProfileRepository;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.database.redis.service.ThemeRankingService;
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
public class ThemeServiceImpl implements ThemeService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private ThemeRankingService themeRankingService;

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = new Theme(UUID.randomUUID(), request.getAuthorId(), request.getTitle(), Instant.now());
        Theme newTheme = themeRepository.save(theme);
        return ThemeMapper.fromTheme(newTheme);
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream().map(ThemeMapper::fromTheme).toList();
    }

    public List<ThemeResponse> getLikedThemes(UUID authorId) {
        List<UUID> likedThemesIds = profileRepository.getLikedThemesIds(authorId);
        return themeRepository.findAllById(likedThemesIds).stream()
                .map(ThemeMapper::fromTheme)
                .toList();
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
        themeRankingService.recordThemeActivity(request.getThemeId());
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
        themeRankingService.removeThemeActivity(request.getThemeId());
    }

    public ThemeResponse getThemeById(UUID themeId) {
        return themeRepository.findByThemeId(themeId).stream().map(ThemeMapper::fromThemeById).findFirst().orElse(null);
    }
}
