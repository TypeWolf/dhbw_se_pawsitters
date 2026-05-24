package com.dhbw.pawsitters.dto.pet;

import com.dhbw.pawsitters.dto.user.UserSummaryResponse;

public record PetResponse(
        Long id,
        String name,
        String species,
        String breed,
        Integer age,
        UserSummaryResponse owner
) {
}
