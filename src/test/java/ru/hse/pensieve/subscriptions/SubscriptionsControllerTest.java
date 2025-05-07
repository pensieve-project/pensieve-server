package ru.hse.pensieve.subscriptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.subscriptions.models.SubscriptionRequest;
import ru.hse.pensieve.subscriptions.routes.SubscriptionsController;
import ru.hse.pensieve.subscriptions.service.SubscriptionsService;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = SubscriptionsController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class SubscriptionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionsService subscriptionsService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getSubscriptions() throws Exception {
        UUID subscriberId = UUID.randomUUID();

        UUID targetId = UUID.randomUUID();

        when(subscriptionsService.getSubscriptions(subscriberId)).thenReturn(List.of(targetId));

        String expectedJson = objectMapper.writeValueAsString(List.of(targetId));

        mockMvc.perform(get("/subscriptions/subscriptions")
                        .param("subscriberId", subscriberId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void getSubscriptionsCount() throws Exception {
        UUID subscriberId = UUID.randomUUID();
        int expectedCount = 5;

        when(subscriptionsService.getSubscriptionsCount(subscriberId)).thenReturn(expectedCount);

        mockMvc.perform(get("/subscriptions/subscriptions-count")
                        .param("subscriberId", subscriberId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedCount)));
    }

    @Test
    public void getSubscribers() throws Exception {
        UUID targetId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        when(subscriptionsService.getSubscribers(targetId)).thenReturn(List.of(subscriberId));

        mockMvc.perform(get("/subscriptions/subscribers")
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(subscriberId.toString()));
    }

    @Test
    public void getSubscribersCount() throws Exception {
        UUID targetId = UUID.randomUUID();
        int expectedCount = 3;

        when(subscriptionsService.getSubscribersCount(targetId)).thenReturn(expectedCount);

        mockMvc.perform(get("/subscriptions/subscribers-count")
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(expectedCount)));
    }

    @Test
    public void subscribe() throws Exception {
        UUID subscriberId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(subscriberId, targetId);

        mockMvc.perform(post("/subscriptions/subscribe")
                        .contentType("application/json")
                        .content("{\"subscriberId\":\"" + subscriberId + "\",\"targetId\":\"" + targetId + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void unsubscribe() throws Exception {
        UUID subscriberId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        SubscriptionRequest request = new SubscriptionRequest(subscriberId, targetId);

        mockMvc.perform(delete("/subscriptions/unsubscribe")
                        .contentType("application/json")
                        .content("{\"subscriberId\":\"" + subscriberId + "\",\"targetId\":\"" + targetId + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    public void hasUserSubscribed_whenSubscribed() throws Exception {
        UUID subscriberId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(subscriptionsService.hasUserSubscribed(Mockito.any())).thenReturn(true);

        mockMvc.perform(get("/subscriptions/subscribed")
                        .param("subscriberId", subscriberId.toString())
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void hasUserSubscribed_whenNotSubscribed() throws Exception {
        UUID subscriberId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        when(subscriptionsService.hasUserSubscribed(Mockito.any())).thenReturn(false);

        mockMvc.perform(get("/subscriptions/subscribed")
                        .param("subscriberId", subscriberId.toString())
                        .param("targetId", targetId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
