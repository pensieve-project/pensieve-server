package ru.hse.pensieve.authentication.service;

import java.util.concurrent.CompletableFuture;
import ru.hse.pensieve.authentication.models.*;

public interface AuthenticationService {

    CompletableFuture<AuthenticationResponse> login(AuthenticationRequest request);

    CompletableFuture<AuthenticationResponse> register(RegisterRequest request);

    Tokens getNewTokens(String refreshToken);
}
