package ru.hse.pensieve.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.hse.pensieve.authentication.models.Tokens;
import ru.hse.pensieve.authentication.service.JwtService;
import ru.hse.pensieve.authentication.service.JwtServiceImpl;
import ru.hse.pensieve.database.postgres.models.User;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    private final String rawAccessSecret = "supersecretkeyforaccesstoken12345678901234567890";
    private final String rawRefreshSecret = "supersecretkeyforrefreshtoken09876543210987654321";

    private User testUser;

    @BeforeEach
    public void setup() {
        String accessSecret = Base64.getEncoder().encodeToString(rawAccessSecret.getBytes());
        String refreshSecret = Base64.getEncoder().encodeToString(rawRefreshSecret.getBytes());

        jwtService = new JwtServiceImpl(accessSecret, refreshSecret);

        testUser = new User();
        testUser.setUsername("testuser");
    }

    @Test
    public void testGenerateAndValidateAccessToken() {
        String token = jwtService.generateAccessToken(testUser);
        assertNotNull(token);
        assertTrue(jwtService.validateAccessToken(token));

        Claims claims = jwtService.getAccessClaims(token);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    public void testGenerateAndValidateRefreshToken() {
        String token = jwtService.generateRefreshToken(testUser);
        assertNotNull(token);
        assertTrue(jwtService.validateRefreshToken(token));

        Claims claims = jwtService.getRefreshClaims(token);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    public void testGenerateNewTokensFromRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        Tokens newTokens = jwtService.generateTokens(refreshToken);

        assertNotNull(newTokens);
        assertTrue(jwtService.validateAccessToken(newTokens.getAccessToken()));
        assertTrue(jwtService.validateRefreshToken(newTokens.getRefreshToken()));
    }

    @Test
    public void testInvalidAccessToken() {
        String invalidToken = "some.invalid.token";
        assertFalse(jwtService.validateAccessToken(invalidToken));
    }

    @Test
    public void testInvalidSignatureToken() {
        SecretKey fakeSecret = Keys.hmacShaKeyFor("anothersecretkey12345678901234567890".getBytes());
        String forgedToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 5))
                .signWith(fakeSecret)
                .compact();

        assertFalse(jwtService.validateAccessToken(forgedToken));
    }

    @Test
    public void testExpiredAccessToken() {
        String expiredToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(rawAccessSecret.getBytes()))
                .compact();

        assertFalse(jwtService.validateAccessToken(expiredToken));
    }

    @Test
    public void testSmallAccessToken() throws InterruptedException {
        String smallToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 5000))
                .signWith(Keys.hmacShaKeyFor(rawAccessSecret.getBytes()))
                .compact();

        assertTrue(jwtService.validateAccessToken(smallToken));

        TimeUnit.SECONDS.sleep(6);

        assertFalse(jwtService.validateAccessToken(smallToken));
    }

    @Test
    public void testSmallRefreshToken() throws InterruptedException {
        String smallToken = Jwts.builder()
                .setSubject("testuser")
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 5000))
                .signWith(Keys.hmacShaKeyFor(rawRefreshSecret.getBytes()))
                .compact();

        assertTrue(jwtService.validateRefreshToken(smallToken));

        TimeUnit.SECONDS.sleep(6);

        assertFalse(jwtService.validateRefreshToken(smallToken));
    }
}
