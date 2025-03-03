package ru.hse.pensieve.themes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.models.ThemeKey;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.themes.models.ThemeRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Autowired
    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme createTheme(ThemeRequest request) {
        ThemeKey key = new ThemeKey(UUID.randomUUID(), request.getAuthorId());
        Theme theme = new Theme(key, request.getTitle(), Instant.now());
        return themeRepository.save(theme);
    }

    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }

}
