package com.dhbw.pawsitters.service.admin;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.user.Role;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser admin;
    private AppUser user;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.payment.Payment.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.sitting.SittingRequest.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.pet.Pet.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.wallet.Wallet.class);
        unitOfWork.deleteAll(AppUser.class);

        admin = unitOfWork.save(AppUser.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.com")
                .password("hash")
                .roles(EnumSet.of(Role.ADMIN))
                .build());

        user = unitOfWork.save(AppUser.builder()
                .firstName("Regular")
                .lastName("User")
                .email("user@test.com")
                .password("hash")
                .roles(EnumSet.of(Role.PET_OWNER))
                .build());
    }

    @Test
    void testRequireAdminHappyPath() {
        assertDoesNotThrow(() -> adminService.requireAdmin(admin.getId()));
    }

    @Test
    void testRequireAdminThrowsForNonAdmin() {
        assertThrows(RuntimeException.class, () -> adminService.requireAdmin(user.getId()));
    }

    @Test
    void testSetRolesHappyPath() {
        AppUser updated = adminService.setRoles(admin.getId(), user.getId(), Set.of(Role.ADMIN, Role.SITTER));
        assertTrue(updated.getRoles().contains(Role.ADMIN));
        assertTrue(updated.getRoles().contains(Role.SITTER));
    }

    @Test
    void testSetRolesByNonAdminThrows() {
        assertThrows(RuntimeException.class, () -> adminService.setRoles(user.getId(), user.getId(), Set.of(Role.ADMIN)));
    }
}
