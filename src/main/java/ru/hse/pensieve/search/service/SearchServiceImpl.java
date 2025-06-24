package ru.hse.pensieve.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.hse.pensieve.database.elk.elasticsearch.models.EsPostDocument;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsThemeDocument;
import ru.hse.pensieve.database.elk.elasticsearch.models.EsUserDocument;
import ru.hse.pensieve.posts.models.PostMapper;
import ru.hse.pensieve.posts.models.PostResponse;
import ru.hse.pensieve.posts.service.PostService;
import ru.hse.pensieve.search.models.EsNotFoundException;
import ru.hse.pensieve.search.models.UserMapper;
import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.themes.models.ThemeMapper;
import ru.hse.pensieve.themes.models.ThemeResponse;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private PostService postService;

    public List<UserResponse> searchUsers(String prefix) throws EsNotFoundException {

        SearchRequest request = SearchRequest.of(s -> s
                .index("users_index")
                .suggest(su -> su
                        .suggesters("authorusername-suggest", fs -> fs
                                .text(prefix)
                                .completion(CompletionSuggester.of(c -> c
                                        .field("suggest")
                                        .size(100)
                                ))
                        )
                )
        );

        try {
            SearchResponse<EsUserDocument> response = client.search(request, EsUserDocument.class);

            return response.suggest()
                    .get("authorusername-suggest")
                    .getFirst()
                    .completion()
                    .options()
                    .stream()
                    .map(CompletionSuggestOption::source)
                    .filter(Objects::nonNull)
                    .map(UserMapper::fromEs)
                    .collect(Collectors.toList());

        } catch (IOException ex) {
            throw new EsNotFoundException("ElasticsearchClient cannot search users with prefix: " + prefix);
        }
    }

    public List<ThemeResponse> searchThemes(String query) throws EsNotFoundException {
        try {
            SearchResponse<EsThemeDocument> response = client.search(s -> s
                            .index("themes_index")
                            .query(q -> q
                                    .match(m -> m
                                            .field("themetitle")
                                            .query(query)
                                            .fuzziness("AUTO")
                                    )
                            ),
                    EsThemeDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(ThemeMapper::fromEsTheme)
                    .toList();

        } catch (IOException ex) {
            throw new EsNotFoundException("ElasticsearchClient cannot search themes with query: " + query);
        }
    }

    public List<PostResponse> searchPosts(String query) throws EsNotFoundException {
        try {
            Query mm = Query.of(q -> q
                .multiMatch(mmq -> mmq
                    .fields("themetitle^3", "text^2", "placename^2", "authorusername^1.5")
                    .query(query)
                    .fuzziness("AUTO")
                )
            );

            SearchResponse<EsPostDocument> response = client.search(s -> s
                .index("posts_index")
                .query(mm)
                .sort(so -> so.score(sc -> sc.order(SortOrder.Desc))),
                EsPostDocument.class
            );

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .map(esPost -> {
                        byte[] photo = postService.getPhoto(esPost.getPostId());
                        Set<UUID> coAuthors = postService.getCoAuthors(esPost.getPostId());

                        return PostMapper.fromEsPost(esPost, photo, coAuthors);
                    })
                    .toList();

        } catch (IOException ex) {
            throw new EsNotFoundException("ElasticsearchClient cannot search posts with query: " + query);
        }
    }
}
