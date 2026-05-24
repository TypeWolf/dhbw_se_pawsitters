package com.dhbw.pawsitters.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name must be 80 characters or fewer")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name must be 80 characters or fewer")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Use a valid email address")
        @Size(max = 320, message = "Email must be 320 characters or fewer")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 10, max = 128, message = "Password must be between 10 and 128 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "Password needs uppercase, lowercase, and a number"
        )
        String password,

        @Size(max = 40, message = "Phone number must be 40 characters or fewer")
        String phoneNumber
) {
}
