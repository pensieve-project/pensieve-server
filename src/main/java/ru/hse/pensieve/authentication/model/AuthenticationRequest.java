package ru.hse.pensieve.authentication.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class AuthenticationRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}
