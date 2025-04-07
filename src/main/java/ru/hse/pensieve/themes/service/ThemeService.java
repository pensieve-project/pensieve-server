package ru.hse.pensieve.themes.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.cassandra.models.Theme;
import ru.hse.pensieve.database.cassandra.models.ThemeKey;
import ru.hse.pensieve.database.cassandra.repositories.ThemeRepository;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsThemeDocument;
import ru.hse.pensieve.themes.models.ThemeMapper;
import ru.hse.pensieve.themes.models.ThemeRequest;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ThemeService {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ElasticsearchClient esClient;

    public ThemeResponse createTheme(ThemeRequest request) {
        ThemeKey key = new ThemeKey(UUID.randomUUID(), request.getAuthorId());
        Theme theme = new Theme(key, request.getTitle(), Instant.now());
        Theme newTheme = themeRepository.save(theme);
        return ThemeMapper.fromTheme(newTheme);
    }

    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAll().stream().map(ThemeMapper::fromTheme).toList();
    }

    public List<ThemeResponse> searchThemes(String query) throws IOException {
        SearchResponse<EsThemeDocument> response = esClient.search(s -> s
                        .index("themes_index")
                        .query(q -> q
                                .match(m -> m
                                        .field("title")
                                        .query(query)
                                )
                        ),
                EsThemeDocument.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(ThemeMapper::fromEsTheme)
                .toList();
    }
}
