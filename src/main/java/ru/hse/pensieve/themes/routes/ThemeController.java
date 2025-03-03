package ru.hse.pensieve.themes.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.service.ThemeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    private final ThemeService themeService;

    @Autowired
    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    @PostMapping
    public Theme createTheme(@RequestBody ThemeRequest request) {
        return themeService.createTheme(request);
    }

    @GetMapping
    public List<Theme> getAllThemes() {
        return themeService.getAllThemes();
    }
}
