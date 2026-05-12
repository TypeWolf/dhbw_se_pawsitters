package com.dhbw.pawsitters.service;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.pet.PetRepository;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UnitOfWorkTest {

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SittingRequestRepository requestRepository;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGenericSaveAndGetById() {
        AppUser user = AppUser.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@test.com")
                .password("hash")
                .build();

        // Test generic save
        AppUser savedUser = unitOfWork.save(user);
        assertNotNull(savedUser.getId());

        // Test generic getById
        AppUser foundUser = unitOfWork.getById(AppUser.class, savedUser.getId());
        assertEquals("Alice", foundUser.getFirstName());
    }

    @Test
    void testGenericGetAll() {
        unitOfWork.save(AppUser.builder().firstName("U1").lastName("L1").email("e1@t.com").password("p").build());
        unitOfWork.save(AppUser.builder().firstName("U2").lastName("L2").email("e2@t.com").password("p").build());

        List<AppUser> users = unitOfWork.getAll(AppUser.class);
        assertEquals(2, users.size());
    }

    @Test
    void testGenericDelete() {
        AppUser user = unitOfWork.save(AppUser.builder().firstName("D").lastName("L").email("d@t.com").password("p").build());
        Long id = user.getId();

        unitOfWork.delete(AppUser.class, id);

        assertThrows(RuntimeException.class, () -> unitOfWork.getById(AppUser.class, id));
    }

    @Test
    void testGetAllByUser() {
        AppUser owner = unitOfWork.save(AppUser.builder().firstName("O").lastName("L").email("o@t.com").password("p").build());
        
        Pet pet1 = Pet.builder().name("P1").species("Dog").owner(owner).build();
        Pet pet2 = Pet.builder().name("P2").species("Cat").owner(owner).build();
        
        unitOfWork.save(pet1);
        unitOfWork.save(pet2);

        // Test the generic getAllByUser method
        List<Pet> pets = unitOfWork.getAllByUser(Pet.class, owner, "owner");
        assertEquals(2, pets.size());
        assertTrue(pets.stream().anyMatch(p -> p.getName().equals("P1")));
        assertTrue(pets.stream().anyMatch(p -> p.getName().equals("P2")));
    }
}
