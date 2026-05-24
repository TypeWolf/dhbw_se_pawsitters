package com.dhbw.pawsitters.controller.auth;

import com.dhbw.pawsitters.dto.auth.LoginRequest;
import com.dhbw.pawsitters.dto.auth.RegisterRequest;
import com.dhbw.pawsitters.dto.user.UserResponse;
import com.dhbw.pawsitters.exception.TooManyRequestsException;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.user.AppUserService;
import com.dhbw.pawsitters.service.user.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AppUserService userService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final ApiMapper mapper;

    public AuthController(
            AppUserService userService,
            AuthenticationManager authenticationManager,
            LoginAttemptService loginAttemptService,
            ApiMapper mapper
    ) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.mapper = mapper;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = userService.register(request);
        log.info("Registered new user id={}", user.getId());
        return mapper.toUserResponse(user);
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String email = AppUserService.normalizeEmail(loginRequest.email());
        loginAttemptService.assertNotBlocked(email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.password())
            );

            request.getSession(true);
            request.changeSessionId();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            loginAttemptService.loginSucceeded(email);
            AppUser user = userService.getUserByEmail(email);
            log.info("User id={} logged in", user.getId());
            return mapper.toUserResponse(user);
        } catch (BadCredentialsException exception) {
            loginAttemptService.loginFailed(email);
            throw exception;
        } catch (TooManyRequestsException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            loginAttemptService.loginFailed(email);
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        );
    }
}
