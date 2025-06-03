package ru.hse.pensieve.authentication;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hse.pensieve.authentication.encrypting.Hasher;
import ru.hse.pensieve.authentication.models.AuthenticationRequest;
import ru.hse.pensieve.authentication.models.AuthenticationResponse;
import ru.hse.pensieve.authentication.models.RegisterRequest;
import ru.hse.pensieve.authentication.models.Tokens;
import ru.hse.pensieve.authentication.service.AuthenticationServiceImpl;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.database.postgres.models.User;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Hasher hasher;

    @Mock
    private JwtService jwtService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String testUsername = "testuser";
    private final UUID userId = UUID.randomUUID();

    @Test
    void login_Success() {
        AuthenticationRequest request = new AuthenticationRequest(testEmail, testPassword);
        String salt = "salt";
        String hashedPassword = "hashedPassword";
        User user = new User(testUsername, testEmail, hashedPassword, salt);
        user.setId(userId);

        when(userRepository.findSaltByEmail(testEmail)).thenReturn(Optional.of(salt));
        when(hasher.hashWithSha256(testPassword + salt)).thenReturn(hashedPassword);
        when(userRepository.findUserByEmailAndPasswordHash(testEmail, hashedPassword))
                .thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        CompletableFuture<AuthenticationResponse> future = authenticationService.login(request);
        AuthenticationResponse response = future.join();

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(testUsername, response.getUsername());
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(userRepository).updateRefreshTokenById(userId, "refreshToken");
    }

    @Test
    void login_EmailNotFound() {
        AuthenticationRequest request = new AuthenticationRequest(testEmail, testPassword);
        when(userRepository.findSaltByEmail(testEmail)).thenReturn(Optional.empty());

        CompletableFuture<AuthenticationResponse> future = authenticationService.login(request);
        assertThrows(RuntimeException.class, future::join, "Email not found");
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest(testUsername, testEmail, testPassword);
        User savedUser = new User(testUsername, testEmail, "hashedPassword", "salt");
        savedUser.setId(userId);

        when(userRepository.existsUserByUsername(testUsername)).thenReturn(false);
        when(userRepository.existsUserByEmail(testEmail)).thenReturn(false);
        when(hasher.hashWithSha256(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(Mockito.any())).thenReturn(savedUser);
        when(jwtService.generateAccessToken(Mockito.any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(Mockito.any())).thenReturn("refreshToken");

        CompletableFuture<AuthenticationResponse> future = authenticationService.register(request);
        AuthenticationResponse response = future.join();

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(testUsername, response.getUsername());
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(testUsername) &&
                        user.getEmail().equals(testEmail) &&
                        user.getPasswordHash().equals("hashedPassword") &&
                        user.getSalt() != null
        ));
    }

    @Test
    void register_UsernameExists() {
        RegisterRequest request = new RegisterRequest(testUsername, testEmail, testPassword);
        when(userRepository.existsUserByUsername(testUsername)).thenReturn(true);

        CompletableFuture<AuthenticationResponse> future = authenticationService.register(request);
        assertThrows(RuntimeException.class, future::join, "Username " + testUsername + " already exists");
    }

    @Test
    void getNewTokens_Success() {
        String oldRefreshToken = "oldRefreshToken";
        String newRefreshToken = "newRefreshToken";
        String newAccessToken = "newAccessToken";
        Tokens expectedTokens = new Tokens(newAccessToken, newRefreshToken);

        when(userRepository.existsByRefreshToken(oldRefreshToken)).thenReturn(true);
        when(jwtService.validateRefreshToken(oldRefreshToken)).thenReturn(true);
        when(jwtService.generateTokens(oldRefreshToken)).thenReturn(expectedTokens);

        Tokens result = authenticationService.getNewTokens(oldRefreshToken);

        assertEquals(expectedTokens, result);
        verify(userRepository).updateRefreshToken(oldRefreshToken, newRefreshToken);
    }

    @Test
    void getNewTokens_InvalidToken() {
        String invalidToken = "invalidToken";
        when(userRepository.existsByRefreshToken(invalidToken)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authenticationService.getNewTokens(invalidToken),
                "You need to login again"
        );
    }

    @Test
    void getNewTokens_ExpiredToken() {
        String expiredToken = "expiredToken";
        when(userRepository.existsByRefreshToken(expiredToken)).thenReturn(true);
        when(jwtService.validateRefreshToken(expiredToken)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> authenticationService.getNewTokens(expiredToken),
                "You need to login again"
        );
    }
}
