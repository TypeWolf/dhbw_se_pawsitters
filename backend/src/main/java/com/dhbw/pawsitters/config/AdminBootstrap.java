package com.dhbw.pawsitters.config;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.repository.user.AppUserRepository;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${pawsitters.admin.email:}") String adminEmail,
            @Value("${pawsitters.admin.password:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        String normalizedEmail = AppUserService.normalizeEmail(adminEmail);
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        AppUser admin = AppUser.builder()
                .firstName("Platform")
                .lastName("Admin")
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(AppUser.Role.ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Created bootstrap admin account id={}", admin.getId());
    }
}
