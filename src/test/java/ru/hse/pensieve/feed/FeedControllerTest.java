package ru.hse.pensieve.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.feed.routes.FeedController;
import ru.hse.pensieve.feed.service.FeedService;
import ru.hse.pensieve.posts.models.PostResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = FeedController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeedService feedService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getSubscriptionsFeed() throws Exception {
        UUID userId = UUID.randomUUID();
        Integer limit = 10;
        Instant lastSeenTime = Instant.now();

        PostResponse mockResponse = new PostResponse();
        mockResponse.setText("Some post");

        when(feedService.getSubscriptionsFeed(userId, limit, lastSeenTime)).thenReturn(List.of(mockResponse));

        mockMvc.perform(get("/feed/subscriptions")
                        .param("userId", userId.toString())
                        .param("limit", limit.toString())
                        .param("lastSeenTime", lastSeenTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text", is("Some post")));
    }
}
