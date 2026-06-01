package com.dhbw.pawsitters.controller.user;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private AppUserService userService;

    @GetMapping
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    public AppUser createUser(@RequestBody AppUser user) {
        return userService.register(user);
    }
    @CrossOrigin(origins = "*")
    @PutMapping("/{id}/address")
    public AppUser updateAddress(@PathVariable Long id, @RequestBody AppUser user) {
        AppUser existing = userService.getUserById(id);
        existing.setAddress(user.getAddress());
        return userService.saveUser(existing);
    }

}
