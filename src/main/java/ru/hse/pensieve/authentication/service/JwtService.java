package ru.hse.pensieve.authentication.service;

import ru.hse.pensieve.authentication.models.Tokens;
import ru.hse.pensieve.database.postgres.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtService {

    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;

    public JwtService(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}") String jwtRefreshSecret
    ) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
    }

    public String generateAccessToken(@NonNull User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 5))
            .signWith(jwtAccessSecret)
            .compact();
    }

    public String generateRefreshToken(@NonNull User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30))
            .signWith(jwtRefreshSecret)
            .compact();
    }

    public Tokens generateTokens(@NonNull String refreshToken) {
        Claims claims = getRefreshClaims(refreshToken);
        String username = claims.getSubject();
        String newAccessToken = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 5))
                .signWith(jwtAccessSecret)
                .compact();
        String newRefreshToken = Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30))
                .signWith(jwtRefreshSecret)
                .compact();
        return new Tokens(newAccessToken, newRefreshToken);
    }

    public boolean validateAccessToken(@NonNull String accessToken) {
        return validateToken(accessToken, jwtAccessSecret);
    }

    public boolean validateRefreshToken(@NonNull String refreshToken) {
        return validateToken(refreshToken, jwtRefreshSecret);
    }

    private boolean validateToken(@NonNull String token, @NonNull Key secret) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.error("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.error("Malformed jwt");
        } catch (SignatureException sEx) {
            log.error("Invalid signature");
        } catch (Exception e) {
            log.error("invalid token");
        }
        return false;
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}