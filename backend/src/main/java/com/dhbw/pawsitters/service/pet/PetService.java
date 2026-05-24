package com.dhbw.pawsitters.service.pet;

import com.dhbw.pawsitters.dto.pet.PetCreateRequest;
import com.dhbw.pawsitters.exception.NotFoundException;
import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.pet.PetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Pet> getPetsForOwner(AppUser owner) {
        return petRepository.findByOwnerIdOrderByNameAsc(owner.getId());
    }

    @Transactional
    public Pet createPet(PetCreateRequest request, AppUser owner) {
        Pet pet = Pet.builder()
                .name(request.name().trim())
                .species(request.species().trim())
                .breed(cleanNullable(request.breed()))
                .age(request.age())
                .owner(owner)
                .build();
        Pet saved = petRepository.save(pet);
        log.info("User id={} created pet id={}", owner.getId(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PET_NOT_FOUND", "Pet was not found."));
    }

    @Transactional(readOnly = true)
    public Pet getOwnedPet(Long petId, AppUser owner) {
        return petRepository.findByIdAndOwnerId(petId, owner.getId())
                .orElseThrow(() -> new NotFoundException("PET_NOT_FOUND", "Pet was not found for this account."));
    }

    private static String cleanNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
