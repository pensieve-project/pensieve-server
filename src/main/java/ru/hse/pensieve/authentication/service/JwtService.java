package ru.hse.pensieve.authentication.service;

import ru.hse.pensieve.authentication.models.Tokens;
import ru.hse.pensieve.database.postgres.models.User;
import io.jsonwebtoken.Claims;
import org.springframework.lang.NonNull;

public interface JwtService {

    String generateAccessToken(@NonNull User user);

    String generateRefreshToken(@NonNull User user);

    Tokens generateTokens(@NonNull String refreshToken);

    boolean validateAccessToken(@NonNull String accessToken);

    boolean validateRefreshToken(@NonNull String refreshToken);

    Claims getAccessClaims(@NonNull String token);

    Claims getRefreshClaims(@NonNull String token);
}