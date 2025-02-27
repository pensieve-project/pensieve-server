package ru.hse.pensieve.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshRequest {
    private Integer userId;
    private String refreshToken;
}
