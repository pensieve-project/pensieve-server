package ru.hse.pensieve.themes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.models.ThemeKey;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.themes.models.ThemeMapper;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ThemeService {

    @Autowired
    private ThemeRepository themeRepository;

    public ThemeResponse createTheme(ThemeRequest request) {
        ThemeKey key = new ThemeKey(UUID.randomUUID(), request.getAuthorId());
        Theme theme = new Theme(key, request.getTitle(), Instant.now());
        Theme newTheme = themeRepository.save(theme);
        return ThemeMapper.fromTheme(newTheme);
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream().map(ThemeMapper::fromTheme).toList();
    }

}
