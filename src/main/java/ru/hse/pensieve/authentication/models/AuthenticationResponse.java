package ru.hse.pensieve.authentication.models;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private UUID id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
