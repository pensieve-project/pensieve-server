package ru.hse.pensieve.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.search.models.UserResponse;
import ru.hse.pensieve.search.service.SearchService;
import ru.hse.pensieve.search.routes.SearchController;
import ru.hse.pensieve.themes.models.ThemeResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SearchController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SearchService searchService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void searchUsers() throws Exception {
        String query = "user";

        UUID userId = UUID.randomUUID();
        UserResponse mockResponse = new UserResponse(userId);

        when(searchService.searchUsers(query)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/search/users")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId", is(userId.toString())));
    }

    @Test
    public void searchThemes() throws Exception {
        String query = "theme";

        UUID themeId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        String title = "title";
        Instant timeStamp = Instant.now();

        ThemeResponse mockResponse = new ThemeResponse(themeId, authorId, title, timeStamp);

        when(searchService.searchThemes(query)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/search/themes")
                        .param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].themeId", is(themeId.toString())))
                .andExpect(jsonPath("$[0].authorId", is(authorId.toString())))
                .andExpect(jsonPath("$[0].title", is(title)))
                .andExpect(jsonPath("$[0].timeStamp", is(timeStamp.toString())));
    }
}
