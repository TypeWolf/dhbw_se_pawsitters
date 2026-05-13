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

    @Autowired
    private com.dhbw.pawsitters.service.rating.RatingService ratingService;

    public AppUser register(AppUser user) {
        validatePassword(user.getPassword());

        // Check if email exists using generic getByProperty
        boolean exists = !unitOfWork.getByProperty(AppUser.class, "email", user.getEmail()).isEmpty();
        
        if (exists) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(EnumSet.of(Role.PET_OWNER, Role.SITTER));
        }

        // Bootstrap: the very first registered user also becomes an admin
        if (unitOfWork.count(AppUser.class) == 1 && user.getId() == 1L) {
             if (!user.getRoles().contains(Role.ADMIN)) {
                 user.getRoles().add(Role.ADMIN);
                 user = unitOfWork.save(user);
             }
        }

        ratingService.populateAverageRating(user);
        return user;
    }

    public AppUser getUserByEmail(String email) {
        AppUser user = unitOfWork.getByProperty(AppUser.class, "email", email).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        ratingService.populateAverageRating(user);
        return user;
    }

    public List<AppUser> getAllUsers() {
        List<AppUser> users = unitOfWork.getAll(AppUser.class);
        users.forEach(ratingService::populateAverageRating);
        return users;
    }

    public AppUser getUserById(Long id) {
        AppUser user = unitOfWork.getById(AppUser.class, id);
        ratingService.populateAverageRating(user);
        return user;
    }
}
