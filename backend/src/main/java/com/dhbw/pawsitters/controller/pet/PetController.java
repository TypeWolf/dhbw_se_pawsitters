package com.dhbw.pawsitters.controller.pet;

import com.dhbw.pawsitters.dto.pet.PetResponse;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.service.pet.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/pets")
public class PetController {

    private final PetService petService;
    private final ApiMapper mapper;

    public PetController(PetService petService, ApiMapper mapper) {
        this.petService = petService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PetResponse> getAllPets() {
        return petService.getAllPets().stream()
                .map(mapper::toPetResponse)
                .toList();
    }
}
