package com.dhbw.pawsitters.controller.auth;

import com.dhbw.pawsitters.model.auth.LoginRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AppUserService userService;

    @PostMapping("/register")
    public AppUser register(@RequestBody AppUser user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public AppUser login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
}
