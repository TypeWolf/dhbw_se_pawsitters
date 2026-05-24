package com.dhbw.pawsitters.dto.user;

import com.dhbw.pawsitters.model.user.AppUser;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        AppUser.Role role
) {
}
