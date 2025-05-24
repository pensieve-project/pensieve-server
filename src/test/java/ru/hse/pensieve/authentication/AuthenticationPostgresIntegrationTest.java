package ru.hse.pensieve.authentication;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.hse.pensieve.authentication.models.*;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.database.postgres.models.User;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class AuthenticationPostgresIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testFullRegisterWithPostgres() {
        String username = "user";
        String email = "user@test.com";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, email, password);
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                "/auth/register",
                request,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());

        assertEquals(1, userRepository.count());
        assertTrue(userRepository.findSaltByEmail(email).isPresent());
        assertEquals(username, userRepository.findUsernameById(response.getBody().getId()));
        assertTrue(userRepository.existsUserByUsername(username));
        assertTrue(jwtService.validateAccessToken(response.getBody().getAccessToken()));
        assertTrue(jwtService.validateRefreshToken(response.getBody().getRefreshToken()));
    }

    @Test
    void testLoginWithPostgres() {
        String username = "user";
        String email = "user@test.com";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, email, password);
        restTemplate.postForEntity("/auth/register", request, AuthenticationResponse.class);

        assertEquals(1, userRepository.count());

        AuthenticationRequest loginRequest = new AuthenticationRequest(email, password);
        ResponseEntity<AuthenticationResponse> response = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthenticationResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());

        assertEquals(1, userRepository.count());
        assertTrue(userRepository.findSaltByEmail(email).isPresent());
        assertEquals(username, userRepository.findUsernameById(response.getBody().getId()));
        assertTrue(userRepository.existsUserByUsername(username));
        assertTrue(jwtService.validateAccessToken(response.getBody().getAccessToken()));
        assertTrue(jwtService.validateRefreshToken(response.getBody().getRefreshToken()));
    }

    @Test
    void testSuccessfulTokenRefresh() throws InterruptedException {
        String username = "user";
        String email = "user@test.com";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, email, password);
        ResponseEntity<AuthenticationResponse> registerResponse = restTemplate.postForEntity(
                "/auth/register",
                request,
                AuthenticationResponse.class
        );
        String refreshToken = registerResponse.getBody().getRefreshToken();

        Optional<User> user = userRepository.findById(registerResponse.getBody().getId());
        assertTrue(user.isPresent());
        String oldRefresh = user.get().getRefreshToken();
        assertEquals(refreshToken, oldRefresh);
        assertTrue(jwtService.validateRefreshToken(oldRefresh));

        Thread.sleep(1000);

        ResponseEntity<Tokens> refreshResponse = restTemplate.getForEntity(
                "/auth/token?refreshToken={refreshToken}",
                Tokens.class,
                refreshToken
        );

        assertEquals(HttpStatus.OK, refreshResponse.getStatusCode());

        refreshToken = refreshResponse.getBody().getRefreshToken();
        user = userRepository.findById(registerResponse.getBody().getId());
        assertTrue(user.isPresent());
        String newRefresh = user.get().getRefreshToken();
        assertEquals(refreshToken, newRefresh);
        assertTrue(jwtService.validateRefreshToken(newRefresh));

        assertTrue(jwtService.validateAccessToken(refreshResponse.getBody().getAccessToken()));

        assertNotEquals(oldRefresh, newRefresh);
    }
}