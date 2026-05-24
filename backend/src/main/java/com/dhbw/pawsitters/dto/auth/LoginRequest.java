package com.dhbw.pawsitters.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Use a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(max = 128, message = "Password is too long")
        String password
) {
}
