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
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        return ResponseEntity.ok(themes);
    }

    @GetMapping("/get-liked")
    public ResponseEntity<List<ThemeResponse>> getLikedThemes(@RequestParam UUID authorId) {
        List<ThemeResponse> themes = themeService.getLikedThemes(authorId);
        return ResponseEntity.ok(themes);
    }
  
    @GetMapping("/title")
    public ResponseEntity<String> getThemeTitle(@RequestParam UUID themeId) {
        return ResponseEntity.ok(themeService.getThemeTitle(themeId));
    }

    @GetMapping("/liked")
    public ResponseEntity<Boolean> hasUserLikedTheme(@RequestParam UUID authorId, @RequestParam UUID themeId) {
        return ResponseEntity.ok(themeService.hasUserLikedTheme(new LikeRequest(authorId, themeId)));
    }

    @PostMapping("/like")
    public ResponseEntity<?> likeTheme(@RequestBody LikeRequest request) {
        themeService.likeTheme(request);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/unlike")
    public ResponseEntity<?> unlikeTheme(@RequestParam UUID authorId, @RequestParam UUID themeId) {
        themeService.unlikeTheme(new LikeRequest(authorId, themeId));
        return ResponseEntity.ok().build();
    }
}
