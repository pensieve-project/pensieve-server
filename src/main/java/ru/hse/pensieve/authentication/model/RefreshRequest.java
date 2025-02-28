package ru.hse.pensieve.authentication.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshRequest {
    private UUID userId;
    private String refreshToken;
}
