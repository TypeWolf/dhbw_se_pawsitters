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
        // Check if email exists using generic getByProperty
        boolean exists = !unitOfWork.getByProperty(AppUser.class, "email", user.getEmail()).isEmpty();
        
        if (exists) {
            throw new RuntimeException("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return unitOfWork.save(user);
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
                 return unitOfWork.save(user);
             }
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
