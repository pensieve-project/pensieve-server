package ru.hse.pensieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ru.hse.pensieve.authentication.AuthenticationController;
import ru.hse.pensieve.authentication.model.AuthenticationRequest;
import ru.hse.pensieve.authentication.model.AuthenticationResponse;
import ru.hse.pensieve.authentication.model.RegisterRequest;
import ru.hse.pensieve.authentication.service.AuthenticationService;

public class AuthenticationControllerTest {
    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest("user", "test@gmail.com", "password");
        AuthenticationResponse response = new AuthenticationResponse(1, "user");
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(CompletableFuture.completedFuture(response));

        ResponseEntity<AuthenticationResponse> result = authenticationController.registerUser(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(authenticationService).register(request);
    }

    @Test
    public void testLoginUser_Success() {
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        AuthenticationResponse response = new AuthenticationResponse(1, "user");
        when(authenticationService.login(any(AuthenticationRequest.class))).thenReturn(CompletableFuture.completedFuture(response));

        ResponseEntity<AuthenticationResponse> result = authenticationController.loginUser(request);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(authenticationService).login(request);
    }
}
