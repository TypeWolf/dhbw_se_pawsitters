package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.dto.auth.RegisterRequest;
import com.dhbw.pawsitters.exception.ConflictException;
import com.dhbw.pawsitters.exception.NotFoundException;
import com.dhbw.pawsitters.exception.UnauthorizedException;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("EMAIL_ALREADY_EXISTS", "An account with this email already exists.");
        }

        AppUser user = AppUser.builder()
                .firstName(clean(request.firstName()))
                .lastName(clean(request.lastName()))
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .phoneNumber(cleanNullable(request.phoneNumber()))
                .role(AppUser.Role.USER)
                .build();

        AppUser saved = userRepository.save(user);
        log.info("Created user id={} role={}", saved.getId(), saved.getRole());
        return saved;
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("AUTHENTICATION_REQUIRED", "Please log in to continue.");
        }
        return getUserByEmail(authentication.getName());
    }

    @Transactional(readOnly = true)
    public AppUser getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User was not found."));
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User was not found."));
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String clean(String value) {
        return value.trim();
    }

    private static String cleanNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
