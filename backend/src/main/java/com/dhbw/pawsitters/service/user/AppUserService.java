package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        AppUser user = getUserByEmail(email);
        
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
