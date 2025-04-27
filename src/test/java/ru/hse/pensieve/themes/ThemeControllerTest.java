package ru.hse.pensieve.themes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.themes.models.*;
import ru.hse.pensieve.themes.routes.ThemeController;
import ru.hse.pensieve.themes.service.ThemeService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ThemeController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class ThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateTheme() throws Exception {
        UUID authorId = UUID.randomUUID();
        String title = "Some title";
        ThemeRequest request = new ThemeRequest(authorId, title);

        ThemeResponse response = new ThemeResponse(UUID.randomUUID(), authorId, title, Instant.now());

        when(themeService.createTheme(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/themes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authorId", is(authorId.toString())))
                .andExpect(jsonPath("$.title", is("Some title")));
    }

    @Test
    public void testGetAllThemes() throws Exception {
        ThemeResponse response = new ThemeResponse(UUID.randomUUID(), UUID.randomUUID(), "Some title", Instant.now());

        when(themeService.getAllThemes()).thenReturn(List.of(response));

        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title", is("Some title")));
    }

    @Test
    public void testGetThemeTitle() throws Exception {
        UUID themeId = UUID.randomUUID();

        when(themeService.getThemeTitle(themeId)).thenReturn("Some title");

        mockMvc.perform(get("/themes/title")
                        .param("themeId", themeId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Some title"));
    }
}
