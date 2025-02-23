package ru.hse.pensieve.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import ru.hse.pensieve.authentication.model.AuthenticationRequest;
import ru.hse.pensieve.authentication.model.AuthenticationResponse;
import ru.hse.pensieve.authentication.model.RegisterRequest;
import ru.hse.pensieve.database.models.User;
import ru.hse.pensieve.database.repositories.UserRepository;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    public CompletableFuture<AuthenticationResponse> login(AuthenticationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String salt = userRepository.findSaltByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Salt not found for user: " + request.getUsername()));
            User user = userRepository.findUserByUsernameAndPasswordHash(
                    request.getUsername(), request.getPassword() + salt)
                    .orElseThrow(() -> new RuntimeException("User not found or incorrect password"));
            return new AuthenticationResponse(
                    user.getId(),
                    user.getUsername()
            );
        });
    }

    public CompletableFuture<AuthenticationResponse> register(RegisterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            boolean userExists = userRepository.existsUserByUsername(request.getUsername());
            if (userExists) {
                throw new InsufficientAuthenticationException("Username " + request.getUsername() + " already exists");
            }
            SecureRandom random = new SecureRandom();
            byte[] generatedSalt = new byte[16];
            random.nextBytes(generatedSalt);
            String salt =  Base64.getEncoder().encodeToString(generatedSalt);
            String PasswordHash = request.getPassword() + salt;
            User user = new User(request.getUsername(), PasswordHash, salt);
            User userWithId = userRepository.save(user);
            return new AuthenticationResponse(
                    userWithId.getId(),
                    userWithId.getUsername()
            );
        });
    }
}
