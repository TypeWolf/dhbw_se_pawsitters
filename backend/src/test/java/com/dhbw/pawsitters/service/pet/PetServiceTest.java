package com.dhbw.pawsitters.service.pet;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PetServiceTest {

    @Autowired
    private PetService petService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser owner;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.payment.Payment.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.sitting.SittingRequest.class);
        unitOfWork.deleteAll(Pet.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.wallet.Wallet.class);
        unitOfWork.deleteAll(AppUser.class);

        owner = unitOfWork.save(AppUser.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@test.com")
                .password("hash")
                .build());
    }

    @Test
    void testCreatePet() {
        Pet pet = Pet.builder()
                .name("Buddy")
                .species("Dog")
                .owner(owner)
                .build();

        Pet saved = petService.createPet(pet);
        assertNotNull(saved.getId());
        assertEquals("Buddy", saved.getName());
    }

    @Test
    void testGetAllPets() {
        petService.createPet(Pet.builder().name("P1").species("Dog").owner(owner).build());
        petService.createPet(Pet.builder().name("P2").species("Cat").owner(owner).build());

        List<Pet> pets = petService.getAllPets();
        assertEquals(2, pets.size());
    }

    @Test
    void testGetPetById() {
        Pet saved = petService.createPet(Pet.builder().name("Buddy").species("Dog").owner(owner).build());
        Pet found = petService.getPetById(saved.getId());
        assertEquals("Buddy", found.getName());
    }
}
