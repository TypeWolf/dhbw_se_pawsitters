package com.dhbw.pawsitters.dto.pet;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PetCreateRequest(
        @NotBlank(message = "Pet name is required")
        @Size(max = 80, message = "Pet name must be 80 characters or fewer")
        String name,

        @NotBlank(message = "Species is required")
        @Size(max = 60, message = "Species must be 60 characters or fewer")
        String species,

        @Size(max = 80, message = "Breed must be 80 characters or fewer")
        String breed,

        @Min(value = 0, message = "Age cannot be negative")
        @Max(value = 80, message = "Age looks too high")
        Integer age
) {
}
