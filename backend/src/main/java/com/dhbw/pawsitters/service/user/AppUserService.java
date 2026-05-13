package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import com.dhbw.pawsitters.model.user.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class AppUserService {

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AppUser register(AppUser user) {
        validatePassword(user.getPassword());

        // Check if email exists using generic getByProperty
        boolean exists = !unitOfWork.getByProperty(AppUser.class, "email", user.getEmail()).isEmpty();

        if (exists) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default roles for every new account.
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(EnumSet.of(Role.PET_OWNER, Role.SITTER));
        }

        // Admin bootstrap: if no existing user has ADMIN, the new one gets it.
        // Survives DataInitializer's seed users (they don't carry ADMIN) and
        // doesn't depend on user id == 1L magic numbers.
        boolean adminExists = unitOfWork.getAll(AppUser.class).stream()
                .anyMatch(u -> u.getRoles() != null && u.getRoles().contains(Role.ADMIN));
        if (!adminExists) {
            user.getRoles().add(Role.ADMIN);
        }

        return unitOfWork.save(user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new RuntimeException("Password must be at least 12 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new RuntimeException("Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            throw new RuntimeException("Password must contain at least one special character (!@#$%^&*)");
        }
    }

    public AppUser login(String email, String password) {
        AppUser user = unitOfWork.getByProperty(AppUser.class, "email", email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public AppUser getUserByEmail(String email) {
        return unitOfWork.getByProperty(AppUser.class, "email", email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<AppUser> getAllUsers() {
        return unitOfWork.getAll(AppUser.class);
    }

    public AppUser getUserById(Long id) {
        return unitOfWork.getById(AppUser.class, id);
    }
}
