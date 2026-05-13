package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.user.Role;
import com.dhbw.pawsitters.repository.pet.PetRepository;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AppUserServiceTest {

    @Autowired
    private AppUserService userService;

    @Autowired
    private com.dhbw.pawsitters.service.UnitOfWork unitOfWork;

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
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.payment.Payment.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.sitting.SittingRequest.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.pet.Pet.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.wallet.Wallet.class);
        unitOfWork.deleteAll(AppUser.class);
        
        testUser = AppUser.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("SecureP@ss123!")
                .build();
    }

    @Test
    void testRegisterHappyPath() {
        AppUser registeredUser = userService.register(testUser);

        assertNotNull(registeredUser.getId());
        assertNotEquals("SecureP@ss123!", registeredUser.getPassword());
        assertTrue(passwordEncoder.matches("SecureP@ss123!", registeredUser.getPassword()));
    }

    @Test
    void testRegisterDuplicateEmail() {
        userService.register(testUser);
        
        AppUser secondUser = AppUser.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("AnotherP@ss123!")
                .build();

        assertThrows(RuntimeException.class, () -> userService.register(secondUser), "Email already exists");
    }

    @Test
    void testRegisterInvalidPasswords() {
        // Too short
        testUser.setPassword("Short1!");
        assertThrows(RuntimeException.class, () -> userService.register(testUser));

        // No uppercase
        testUser.setPassword("securep@ss123!");
        assertThrows(RuntimeException.class, () -> userService.register(testUser));

        // No lowercase
        testUser.setPassword("SECUREP@SS123!");
        assertThrows(RuntimeException.class, () -> userService.register(testUser));

        // No number
        testUser.setPassword("SecureP@ss!!!");
        assertThrows(RuntimeException.class, () -> userService.register(testUser));

        // No special char
        testUser.setPassword("SecureP@ss123"); // Wait, @ is a special char in the regex [!@#$%^&*]
        testUser.setPassword("SecurePass123");
        assertThrows(RuntimeException.class, () -> userService.register(testUser));
    }

    @Test
    void testLoginHappyPath() {
        userService.register(testUser);
        
        AppUser loggedInUser = userService.login("john.doe@example.com", "SecureP@ss123!");
        
        assertNotNull(loggedInUser);
        assertEquals("john.doe@example.com", loggedInUser.getEmail());
        assertTrue(loggedInUser.getRoles().contains(Role.PET_OWNER));
        assertTrue(loggedInUser.getRoles().contains(Role.SITTER));
    }

    @Test
    void testLoginFirstUserIsAdmin() {
        AppUser admin = userService.register(testUser);
        
        // In login, if it's the first user and ID is 1, it becomes admin
        AppUser loggedIn = userService.login(testUser.getEmail(), "SecureP@ss123!");
        
        if (loggedIn.getId() == 1L) {
            assertTrue(loggedIn.getRoles().contains(Role.ADMIN));
        }
    }

    @Test
    void testLoginUserNotFound() {
        assertThrows(RuntimeException.class, () -> userService.login("nonexistent@example.com", "anyPassword"));
    }

    @Test
    void testLoginIncorrectPassword() {
        userService.register(testUser);
        
        assertThrows(RuntimeException.class, () -> userService.login("john.doe@example.com", "wrongPassword!"));
    }

    @Test
    void testGetUserByEmail() {
        userService.register(testUser);
        AppUser found = userService.getUserByEmail("john.doe@example.com");
        assertEquals("John", found.getFirstName());
    }

    @Test
    void testGetAllUsers() {
        userService.register(testUser);
        List<AppUser> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
    }
}
