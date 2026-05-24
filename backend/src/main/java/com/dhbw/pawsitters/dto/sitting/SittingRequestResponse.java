package com.dhbw.pawsitters.dto.sitting;

import com.dhbw.pawsitters.dto.pet.PetResponse;
import com.dhbw.pawsitters.dto.user.UserSummaryResponse;
import com.dhbw.pawsitters.model.sitting.SittingRequest;

import java.time.LocalDateTime;

public record SittingRequestResponse(
        Long id,
        PetResponse pet,
        UserSummaryResponse requester,
        UserSummaryResponse sitter,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SittingRequest.RequestStatus status
) {
}
