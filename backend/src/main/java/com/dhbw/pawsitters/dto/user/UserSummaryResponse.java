package com.dhbw.pawsitters.dto.user;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName
) {
}
