package ru.hse.pensieve.search.models;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String username;
}
