package ru.hse.pensieve.themes.routes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.hse.pensieve.themes.models.LikeRequest;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;
import ru.hse.pensieve.themes.service.ThemeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/themes")
public class ThemeController {

    @Autowired
    private ThemeService themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@RequestBody ThemeRequest request) {
        ThemeResponse response = themeService.createTheme(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        return ResponseEntity.ok(themes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ThemeResponse>> searchThemes(@RequestParam("query") String query) {
        try {
            return ResponseEntity.ok(themeService.searchThemes(query));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
  
    @GetMapping("/title")
    public ResponseEntity<String> getThemeTitle(@RequestParam UUID themeId) {
        return ResponseEntity.ok(themeService.getThemeTitle(themeId));
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeTheme(@RequestBody LikeRequest request) {
        themeService.likeTheme(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlike")
    public ResponseEntity<?> unlikeTheme(@RequestBody LikeRequest request) {
        themeService.unlikeTheme(request);
        return ResponseEntity.ok().build();
    }
}
