package com.dhbw.pawsitters.service.user;

import com.dhbw.pawsitters.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCKOUT_DURATION = Duration.ofMinutes(15);

    private final Map<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void assertNotBlocked(String email) {
        AttemptWindow window = attempts.get(email);
        if (window == null) {
            return;
        }

        if (window.lockedUntil != null && window.lockedUntil.isAfter(Instant.now())) {
            throw new TooManyRequestsException(
                    "LOGIN_LOCKED",
                    "Too many failed attempts. Please wait before trying again."
            );
        }

        if (window.lockedUntil != null && window.lockedUntil.isBefore(Instant.now())) {
            attempts.remove(email);
        }
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public void loginFailed(String email) {
        attempts.compute(email, (key, current) -> {
            AttemptWindow window = current == null ? new AttemptWindow(0, null) : current;
            int failures = window.failures + 1;
            Instant lockedUntil = failures >= MAX_ATTEMPTS ? Instant.now().plus(LOCKOUT_DURATION) : null;
            return new AttemptWindow(failures, lockedUntil);
        });
    }

    private record AttemptWindow(int failures, Instant lockedUntil) {
    }
}
