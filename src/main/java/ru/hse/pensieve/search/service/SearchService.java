package ru.hse.pensieve.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsUserDocument;
import ru.hse.pensieve.search.models.UserMapper;
import ru.hse.pensieve.search.models.UserResponse;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private ElasticsearchClient client;

    public List<UserResponse> searchUsers(String prefix) throws IOException {

        SearchRequest request = SearchRequest.of(s -> s
            .index("users_index")
            .suggest(su -> su
                .suggesters("username-suggest", fs -> fs
                    .text(prefix)
                    .completion(CompletionSuggester.of(c -> c
                        .field("suggest")
                    ))
                )
            )
        );

        SearchResponse<EsUserDocument> response = client.search(request, EsUserDocument.class);

        return response.suggest()
                .get("username-suggest")
                .getFirst()
                .completion()
                .options()
                .stream()
                .map(CompletionSuggestOption::source)
                .filter(Objects::nonNull)
                .map(UserMapper::fromEs)
                .collect(Collectors.toList());
    }
}
