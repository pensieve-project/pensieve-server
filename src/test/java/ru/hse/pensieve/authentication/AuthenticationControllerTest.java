package ru.hse.pensieve.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hse.pensieve.authentication.models.AuthenticationRequest;
import ru.hse.pensieve.authentication.models.AuthenticationResponse;
import ru.hse.pensieve.authentication.models.RegisterRequest;
import ru.hse.pensieve.authentication.models.Tokens;
import ru.hse.pensieve.authentication.routes.AuthenticationController;
import ru.hse.pensieve.authentication.service.AuthenticationService;
import ru.hse.pensieve.authentication.service.JwtService;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthenticationController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "user",
                "user@test.com",
                "password123"
        );

        AuthenticationResponse mockResponse = new AuthenticationResponse(
                UUID.randomUUID(),
                "user",
                "access_token",
                "refresh_token"
        );

        when(authenticationService.register(Mockito.any(RegisterRequest.class))).thenReturn(CompletableFuture.completedFuture(mockResponse));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access_token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh_token")))
                .andExpect(jsonPath("$.username", is("user")));
    }

    @Test
    void testLoginUser() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest(
                "user@test.com",
                "password123"
        );

        AuthenticationResponse mockResponse = new AuthenticationResponse(
                UUID.randomUUID(),
                "user",
                "access_token",
                "refresh_token"
        );

        when(authenticationService.login(Mockito.any(AuthenticationRequest.class))).thenReturn(CompletableFuture.completedFuture(mockResponse));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("access_token")))
                .andExpect(jsonPath("$.refreshToken", is("refresh_token")))
                .andExpect(jsonPath("$.username", is("user")));
    }

    @Test
    void testGetNewTokensSuccess() throws Exception {
        String refreshToken = "valid_refresh_token";
        Tokens mockTokens = new Tokens("new_access", "new_refresh");

        when(authenticationService.getNewTokens(refreshToken)).thenReturn(mockTokens);

        mockMvc.perform(get("/auth/token")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("new_access")))
                .andExpect(jsonPath("$.refreshToken", is("new_refresh")));
    }

    @Test
    void testGetNewTokensWithoutToken() throws Exception {
        mockMvc.perform(get("/auth/token")).andExpect(status().isBadRequest());
    }

    @Test
    void testGetNewTokensInvalidToken() throws Exception {
        String invalidToken = "invalid_token";

        when(authenticationService.getNewTokens(invalidToken)).thenReturn(null);

        mockMvc.perform(get("/auth/token")
                        .param("refreshToken", invalidToken))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testGetNewTokensNullToken() throws Exception {
        String invalidToken = null;

        mockMvc.perform(get("/auth/token")
                        .param("refreshToken", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }
}