package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.pet.PetRepository;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppUserServiceTest {

    @Autowired
    private AppUserService userService;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private SittingRequestRepository requestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();
        
        testUser = AppUser.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("plainPassword123")
                .build();
    }

    @Test
    void testRegisterHashesPassword() {
        AppUser registeredUser = userService.register(testUser);

        assertNotNull(registeredUser.getId());
        assertNotEquals("plainPassword123", registeredUser.getPassword());
        assertTrue(passwordEncoder.matches("plainPassword123", registeredUser.getPassword()));
    }

    @Test
    void testLoginWithCorrectPassword() {
        userService.register(testUser);
        
        AppUser loggedInUser = userService.login("john.doe@example.com", "plainPassword123");
        
        assertNotNull(loggedInUser);
        assertEquals("john.doe@example.com", loggedInUser.getEmail());
    }

    @Test
    void testLoginWithIncorrectPassword() {
        userService.register(testUser);
        
        assertThrows(RuntimeException.class, () -> {
            userService.login("john.doe@example.com", "wrongPassword");
        });
    }
}
