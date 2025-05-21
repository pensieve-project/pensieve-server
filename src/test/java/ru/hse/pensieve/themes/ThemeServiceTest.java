package ru.hse.pensieve.themes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.database.cassandra.models.Profile;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.repositories.ProfileRepository;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.themes.models.LikeRequest;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;
import ru.hse.pensieve.themes.service.ThemeServiceImpl;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThemeServiceTest {

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ThemeServiceImpl themeService;

    private final UUID userId = UUID.randomUUID();
    private final UUID themeId = UUID.randomUUID();
    private final String themeTitle = "Test Theme";

    @Test
    void createTheme_ShouldSaveAndReturnTheme() {
        ThemeRequest request = createThemeRequest();

        Theme savedTheme = createTheme();
        when(themeRepository.save(any(Theme.class))).thenReturn(savedTheme);

        ThemeResponse result = themeService.createTheme(request);

        assertNotNull(result);
        assertEquals(themeId, result.getThemeId());
        assertEquals(themeTitle, result.getTitle());
        verify(themeRepository).save(any(Theme.class));
    }

    @Test
    void getAllThemes_ShouldReturnAllThemes() {
        List<Theme> themes = List.of(
                new Theme(themeId, userId, "Theme 1", Instant.now()),
                new Theme(UUID.randomUUID(), userId, "Theme 2", Instant.now())
        );
        when(themeRepository.findAll()).thenReturn(themes);

        List<ThemeResponse> result = themeService.getAllThemes();

        assertEquals(2, result.size());
        assertEquals("Theme 1", result.get(0).getTitle());
        assertEquals("Theme 2", result.get(1).getTitle());
    }

    @Test
    void getLikedThemes_ShouldReturnOnlyLiked() {
        UUID likedThemeId = UUID.randomUUID();
        List<UUID> likedIds = List.of(likedThemeId);
        Theme likedTheme = new Theme(likedThemeId, userId, "Liked Theme", Instant.now());

        when(profileRepository.getLikedThemesIds(userId)).thenReturn(likedIds);
        when(themeRepository.findAllById(likedIds)).thenReturn(List.of(likedTheme));

        List<ThemeResponse> result = themeService.getLikedThemes(userId);

        assertEquals(1, result.size());
        assertEquals("Liked Theme", result.get(0).getTitle());
    }

    @Test
    void getThemeTitle_ShouldReturnTitleWhenExists() {
        Theme theme = createTheme();
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(theme));

        String result = themeService.getThemeTitle(themeId);

        assertEquals(themeTitle, result);
    }

    @Test
    void getThemeTitle_ShouldReturnEmptyWhenNotExists() {
        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());

        String result = themeService.getThemeTitle(themeId);

        assertEquals("", result);
    }

    @Test
    void hasUserLikedTheme_ShouldReturnTrueWhenLiked() {
        LikeRequest request = createLikeRequest();
        when(profileRepository.hasLikedTheme(userId, themeId)).thenReturn(true);

        boolean result = themeService.hasUserLikedTheme(request);

        assertTrue(result);
    }

    @Test
    void likeTheme_ShouldAddToLikedList() {
        LikeRequest request = createLikeRequest();

        Profile profile = createProfile(userId);
        when(profileRepository.findByAuthorId(userId)).thenReturn(profile);
        when(profileRepository.hasLikedTheme(userId, themeId)).thenReturn(false);

        themeService.likeTheme(request);

        assertTrue(profile.getLikedThemesIds().contains(themeId));
        verify(profileRepository).save(profile);
    }

    @Test
    void likeTheme_ShouldNotAddWhenAlreadyLiked() {
        LikeRequest request = createLikeRequest();

        when(profileRepository.hasLikedTheme(userId, themeId)).thenReturn(true);

        themeService.likeTheme(request);

        verify(profileRepository, never()).save(any());
    }

    @Test
    void unlikeTheme_ShouldRemoveFromLikedList() {
        LikeRequest request = createLikeRequest();

        Profile profile = createProfile(userId);
        profile.setLikedThemesIds(new ArrayList<>(List.of(themeId)));
        when(profileRepository.findByAuthorId(userId)).thenReturn(profile);
        when(profileRepository.hasLikedTheme(userId, themeId)).thenReturn(true);

        themeService.unlikeTheme(request);

        assertFalse(profile.getLikedThemesIds().contains(themeId));
        verify(profileRepository).save(profile);
    }

    @Test
    void getThemeById_ShouldReturnThemeWhenExists() {
        Theme theme = createTheme();
        when(themeRepository.findByThemeId(themeId)).thenReturn(List.of(theme));

        ThemeResponse result = themeService.getThemeById(themeId);

        assertNotNull(result);
        assertEquals(themeTitle, result.getTitle());
    }

    @Test
    void getThemeById_ShouldReturnNullWhenNotExists() {
        when(themeRepository.findByThemeId(themeId)).thenReturn(Collections.emptyList());

        ThemeResponse result = themeService.getThemeById(themeId);

        assertNull(result);
    }

    @Test
    void unlikeTheme_ShouldHandleNullLikedList() {
        LikeRequest request = createLikeRequest();

        when(profileRepository.hasLikedTheme(userId, themeId)).thenReturn(false);

        themeService.unlikeTheme(request);

        verify(profileRepository, never()).save(any());
    }

    private Profile createProfile(UUID authorId) {
        return new Profile(
                authorId,
                null,
                "description",
                new ArrayList<>(),
                new ArrayList<>(),
                0,
                0,
                false
        );
    }

    private ThemeRequest createThemeRequest() {
        return new ThemeRequest(userId, themeTitle);
    }

    private Theme createTheme() {
        return new Theme(themeId, userId, themeTitle, Instant.now());
    }

    private LikeRequest createLikeRequest() {
        return new LikeRequest(userId, themeId);
    }
}