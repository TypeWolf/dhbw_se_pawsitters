package com.dhbw.pawsitters.mapper;

import com.dhbw.pawsitters.dto.pet.PetResponse;
import com.dhbw.pawsitters.dto.sitting.SittingRequestResponse;
import com.dhbw.pawsitters.dto.user.UserResponse;
import com.dhbw.pawsitters.dto.user.UserSummaryResponse;
import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import org.springframework.stereotype.Component;

@Component
public class ApiMapper {

    public UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }

    public UserSummaryResponse toUserSummaryResponse(AppUser user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryResponse(user.getId(), user.getFirstName(), user.getLastName());
    }

    public PetResponse toPetResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                toUserSummaryResponse(pet.getOwner())
        );
    }

    public SittingRequestResponse toSittingRequestResponse(SittingRequest request) {
        return new SittingRequestResponse(
                request.getId(),
                toPetResponse(request.getPet()),
                toUserSummaryResponse(request.getRequester()),
                toUserSummaryResponse(request.getSitter()),
                request.getStartTime(),
                request.getEndTime(),
                request.getStatus()
        );
    }
}
