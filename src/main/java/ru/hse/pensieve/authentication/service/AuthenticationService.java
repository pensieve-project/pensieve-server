package ru.hse.pensieve.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import ru.hse.pensieve.authentication.models.*;
import ru.hse.pensieve.database.postgres.models.User;
import ru.hse.pensieve.database.postgres.repositories.UserRepository;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public CompletableFuture<AuthenticationResponse> login(AuthenticationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String salt = userRepository.findSaltByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email not found"));
            User user = userRepository.findUserByEmailAndPasswordHash(
                    request.getEmail(), request.getPassword() + salt)
                    .orElseThrow(() -> new RuntimeException("Wrong password"));
            final String accessToken = jwtService.generateAccessToken(user);
            final String refreshToken = jwtService.generateRefreshToken(user);
            userRepository.updateRefreshTokenById(user.getId(), refreshToken);
            return new AuthenticationResponse(
                    user.getId(),
                    user.getUsername(),
                    accessToken,
                    refreshToken
            );
        });
    }

    public CompletableFuture<AuthenticationResponse> register(RegisterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            boolean userExists = userRepository.existsUserByUsername(request.getUsername());
            if (userExists) {
                throw new RuntimeException("Username " + request.getUsername() + " already exists");
            }
            userExists = userRepository.existsUserByEmail(request.getEmail());
            if (userExists) {
                throw new RuntimeException("Email " + request.getEmail() + " already exists");
            }
            SecureRandom random = new SecureRandom();
            byte[] generatedSalt = new byte[16];
            random.nextBytes(generatedSalt);
            String salt =  Base64.getEncoder().encodeToString(generatedSalt);
            String passwordHash = request.getPassword() + salt;
            User user = new User(request.getUsername(), request.getEmail(), passwordHash, salt);
            final String accessToken = jwtService.generateAccessToken(user);
            final String refreshToken = jwtService.generateRefreshToken(user);
            user.setRefreshToken(refreshToken);
            User userWithId = userRepository.save(user);
            return new AuthenticationResponse(
                    userWithId.getId(),
                    userWithId.getUsername(),
                    accessToken,
                    refreshToken
            );
        });
    }

    public Tokens getNewTokens(String refreshToken) {
        if (userRepository.existsByRefreshToken(refreshToken) && jwtService.validateRefreshToken(refreshToken)) {
            Tokens newTokens = jwtService.generateTokens(refreshToken);
            userRepository.updateRefreshToken(refreshToken, newTokens.getRefreshToken());
            return newTokens;
        }
        throw new RuntimeException("You need to login again");
    }
}
