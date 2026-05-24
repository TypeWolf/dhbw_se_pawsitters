package com.dhbw.pawsitters.controller.user;

import com.dhbw.pawsitters.dto.user.UserResponse;
import com.dhbw.pawsitters.mapper.ApiMapper;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final AppUserService userService;
    private final ApiMapper mapper;

    public UserController(AppUserService userService, ApiMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(mapper::toUserResponse)
                .toList();
    }
}
