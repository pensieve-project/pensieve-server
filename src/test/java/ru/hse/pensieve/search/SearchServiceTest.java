package ru.hse.pensieve.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ShardStatistics;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsThemeDocument;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsUserDocument;
import ru.hse.pensieve.search.models.EsNotFoundException;
import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.search.service.SearchServiceImpl;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ElasticsearchClient client;

    @InjectMocks
    private SearchServiceImpl searchService;

    private final String testQuery = "test";
    private final String indexNameUsers = "users_index";
    private final String indexNameThemes = "themes_index";

    @Test
    void searchUsers_ShouldReturnMappedResults() throws Exception {
        UUID userId = UUID.randomUUID();
        EsUserDocument userDoc = new EsUserDocument(userId, "user1");
        SearchResponse<EsUserDocument> mockResponse = createMockUserResponse(List.of(userDoc), testQuery);

        when(client.search(any(SearchRequest.class), eq(EsUserDocument.class))).thenReturn(mockResponse);

        List<UserResponse> result = searchService.searchUsers(testQuery);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verifyClientCall(indexNameUsers);
    }

    @Test
    void searchUsers_ShouldHandleEmptyResults() throws Exception {
        SearchResponse<EsUserDocument> mockResponse = createMockUserResponse(List.of(), indexNameUsers);
        when(client.search(any(SearchRequest.class), eq(EsUserDocument.class))).thenReturn(mockResponse);

        List<UserResponse> result = searchService.searchUsers(testQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchUsers_ShouldThrowExceptionOnIOException() throws Exception {
        when(client.search(any(SearchRequest.class), eq(EsUserDocument.class))).thenThrow(new IOException("Connection failed"));

        EsNotFoundException ex = assertThrows(EsNotFoundException.class, () -> searchService.searchUsers(testQuery));

        assertTrue(ex.getMessage().contains(testQuery));
    }

    @Test
    void searchThemes_ShouldReturnMappedResults() throws Exception {
        UUID themeId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        EsThemeDocument themeDoc = new EsThemeDocument(themeId, userId, "Test Theme", Instant.now());
        SearchResponse<EsThemeDocument> mockResponse = createMockThemeResponse(List.of(themeDoc));

        when(client.search(any(SearchRequest.class), eq(EsThemeDocument.class))).thenReturn(mockResponse);

        List<ThemeResponse> result = searchService.searchThemes(testQuery);

        assertEquals(1, result.size());
        assertEquals(themeId, result.get(0).getThemeId());
        assertEquals(userId, result.get(0).getAuthorId());
        assertEquals("Test Theme", result.get(0).getTitle());
        verifyClientCall(indexNameThemes);
    }

    @Test
    void searchThemes_ShouldHandleEmptyResults() throws Exception {
        SearchResponse<EsThemeDocument> mockResponse = createMockThemeResponse(List.of());
        when(client.search(any(SearchRequest.class), eq(EsThemeDocument.class))).thenReturn(mockResponse);

        List<ThemeResponse> result = searchService.searchThemes(testQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchThemes_ShouldThrowExceptionOnIOException() throws Exception {
        when(client.search(any(SearchRequest.class), eq(EsThemeDocument.class))).thenThrow(new IOException("Connection failed"));

        EsNotFoundException ex = assertThrows(EsNotFoundException.class, () -> searchService.searchThemes(testQuery));

        assertTrue(ex.getMessage().contains(testQuery));
    }

    @Test
    void searchUsers_ShouldHandlePartialMatch() throws Exception {
        EsUserDocument user1 = new EsUserDocument(UUID.randomUUID(), "testUser");
        EsUserDocument user2 = new EsUserDocument(UUID.randomUUID(), "tester");
        String partialQuery = "test";

        SearchResponse<EsUserDocument> mockResponse = createMockUserResponse(List.of(user1, user2), indexNameThemes);

        when(client.search(any(SearchRequest.class), eq(EsUserDocument.class))).thenReturn(mockResponse);

        List<UserResponse> result = searchService.searchUsers(partialQuery);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getUserId().equals(user1.getUserId())));
        assertTrue(result.stream().anyMatch(u -> u.getUserId().equals(user2.getUserId())));
    }

    @Test
    void searchThemes_ShouldFindSynonyms() throws Exception {
        EsThemeDocument mainTheme = new EsThemeDocument(
                UUID.randomUUID(), UUID.randomUUID(), "so much love", Instant.now()
        );
        EsThemeDocument synonymTheme = new EsThemeDocument(
                UUID.randomUUID(), UUID.randomUUID(), "so much enjoy", Instant.now()
        );

        SearchResponse<EsThemeDocument> mockResponse = createMockThemeResponse(List.of(mainTheme, synonymTheme));

        when(client.search(any(SearchRequest.class), eq(EsThemeDocument.class))).thenReturn(mockResponse);

        List<ThemeResponse> result = searchService.searchThemes("so much passion");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getTitle().equals("so much love")));
        assertTrue(result.stream().anyMatch(t -> t.getTitle().equals("so much enjoy")));
    }

    private void verifyClientCall(String expectedIndex) throws IOException {
        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(client).search(captor.capture(), any(Class.class));

        SearchRequest request = captor.getValue();
        assertEquals(expectedIndex, request.index().getFirst());
    }

    private SearchResponse<EsUserDocument> createMockUserResponse(List<EsUserDocument> documents, String query) {
        return SearchResponse.of(r -> r
                .took(0L)
                .timedOut(false)
                .shards(new ShardStatistics.Builder()
                        .total(1)
                        .successful(1)
                        .failed(0)
                        .build())
                .suggest(Map.of(
                        "username-suggest",
                        List.of(new Suggestion.Builder<EsUserDocument>()
                                .completion(new CompletionSuggest.Builder<EsUserDocument>()
                                        .options(documents.stream().map(this::createOption).toList())
                                        .length(query.length())
                                        .text(query)
                                        .offset(0)
                                        .build())
                                .build())
                ))
                .hits(new HitsMetadata.Builder<EsUserDocument>()
                        .hits(documents.stream().map(doc -> createHit(doc, indexNameUsers)).toList())
                        .build())
        );
    }

    private SearchResponse<EsThemeDocument> createMockThemeResponse(List<EsThemeDocument> documents) {
        return SearchResponse.of(r -> r
                .took(0)
                .timedOut(false)
                .shards(s -> s
                        .total(1)
                        .successful(1)
                        .failed(0)
                )
                .hits(h -> h
                        .hits(documents.stream().map(doc -> createHit(doc, indexNameThemes)).toList())
                )
        );
    }

    private CompletionSuggestOption<EsUserDocument> createOption(EsUserDocument doc) {
        return new CompletionSuggestOption.Builder<EsUserDocument>()
                .text(doc.getUsername())
                .source(doc)
                .score(1.0)
                .build();
    }

    private <T> Hit<T> createHit(T document, String index) {
        return new Hit.Builder<T>()
                .source(document)
                .index(index)
                .id(UUID.randomUUID().toString())
                .build();
    }
}