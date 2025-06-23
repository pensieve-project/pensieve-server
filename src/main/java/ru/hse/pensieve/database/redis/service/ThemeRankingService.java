package ru.hse.pensieve.database.redis.service;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ThemeRankingService {
    private final RedisTemplate<String, Theme> redisThemeTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final ThemeRepository themeRepository;

    private static final String THEME_KEY = "theme:%s";
    private static final String ACTIVITY_KEY = "theme-activity:%s";
    private static final String POPULAR_THEMES_KEY = "popular-themes-24h";

    public void cacheTheme(Theme theme, long ttlHours) {
        redisThemeTemplate.opsForValue().set(
                String.format(THEME_KEY, theme.getThemeId()),
                theme,
                ttlHours,
                TimeUnit.HOURS
        );
    }

    public Theme getCachedTheme(UUID themeId) {
        return redisThemeTemplate.opsForValue().get(String.format(THEME_KEY, themeId));
    }

    public void recordThemeActivity(UUID themeId) {
        long now = System.currentTimeMillis();
        stringRedisTemplate.opsForZSet().add(
                String.format(ACTIVITY_KEY, themeId),
                "activity",
                now
        );
        updateThemeRanking(themeId);
    }

    public void removeThemeActivity(UUID themeId) {
        stringRedisTemplate.opsForZSet().remove(
                String.format(ACTIVITY_KEY, themeId),
                "activity"
        );
        updateThemeRanking(themeId);
    }

    private void updateThemeRanking(UUID themeId) {
        Theme theme = getCachedTheme(themeId);
        if (theme == null) {
            theme = themeRepository.findById(themeId).orElse(null);
            if (theme != null) {
                cacheTheme(theme, 24L);
            }
        }

        long twentyFourHoursAgo = System.currentTimeMillis() - 86_400_000;
        stringRedisTemplate.opsForZSet().removeRangeByScore(
                String.format(ACTIVITY_KEY, themeId),
                0,
                twentyFourHoursAgo
        );

        Long activityCount = stringRedisTemplate.opsForZSet().zCard(
                String.format(ACTIVITY_KEY, themeId)
        );

        if (activityCount != null && activityCount > 0) {
            stringRedisTemplate.opsForZSet().add(
                    POPULAR_THEMES_KEY,
                    themeId.toString(),
                    activityCount
            );
        } else {
            stringRedisTemplate.opsForZSet().remove(
                    POPULAR_THEMES_KEY,
                    themeId.toString()
            );
        }
    }

    public List<Theme> getTopThemes(int limit) {
        Set<String> themeIds = stringRedisTemplate.opsForZSet()
                .reverseRange(POPULAR_THEMES_KEY, 0, limit - 1);

        if (themeIds == null || themeIds.isEmpty()) {
            return List.of();
        }

        return redisThemeTemplate.executePipelined((RedisCallback<Theme>) connection -> {
                    themeIds.forEach(id ->
                            connection.stringCommands().get(
                                    String.format(THEME_KEY, id).getBytes()
                            )
                    );
                    return null;
                }).stream()
                .filter(Objects::nonNull)
                .map(obj -> (Theme) obj)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 60000)
    public void trimPopularThemes() {
        Long size = stringRedisTemplate.opsForZSet().zCard(POPULAR_THEMES_KEY);
        if (size != null && size > 100) {
            stringRedisTemplate.opsForZSet().removeRange(
                    POPULAR_THEMES_KEY,
                    0,
                    size - 100 - 1
            );
        }
    }
}
