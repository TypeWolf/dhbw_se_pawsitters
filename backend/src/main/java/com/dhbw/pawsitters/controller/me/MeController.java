package com.dhbw.pawsitters.controller.me;

import com.dhbw.pawsitters.dto.pet.PetCreateRequest;
import com.dhbw.pawsitters.dto.pet.PetResponse;
import com.dhbw.pawsitters.dto.user.UserResponse;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.pet.PetService;
import com.dhbw.pawsitters.service.user.AppUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final AppUserService userService;
    private final PetService petService;
    private final ApiMapper mapper;

    public MeController(AppUserService userService, PetService petService, ApiMapper mapper) {
        this.userService = userService;
        this.petService = petService;
        this.mapper = mapper;
    }

    @GetMapping
    public UserResponse me(Authentication authentication) {
        return mapper.toUserResponse(userService.getCurrentUser(authentication));
    }

    @GetMapping("/pets")
    public List<PetResponse> myPets(Authentication authentication) {
        AppUser owner = userService.getCurrentUser(authentication);
        return petService.getPetsForOwner(owner).stream()
                .map(mapper::toPetResponse)
                .toList();
    }

    @PostMapping("/pets")
    public PetResponse createPet(@Valid @RequestBody PetCreateRequest request, Authentication authentication) {
        AppUser owner = userService.getCurrentUser(authentication);
        return mapper.toPetResponse(petService.createPet(request, owner));
    }
}
