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

    @Autowired
    private com.dhbw.pawsitters.repository.sitting.SittingRequestRepository sittingRequestRepository;

    @Autowired
    private com.dhbw.pawsitters.service.rating.RatingService ratingService;

    @GetMapping
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/{id}")
    public UserProfileResponse getUserById(@PathVariable Long id) {
        AppUser user = userService.getUserById(id);
        
        long openRequests = sittingRequestRepository.findAll().stream()
                .filter(r -> r.getRequester() != null && r.getRequester().getId().equals(id) && r.getStatus() == com.dhbw.pawsitters.model.sitting.SittingRequest.RequestStatus.PENDING)
                .count();

        long sitsCompleted = sittingRequestRepository.findAll().stream()
                .filter(r -> r.getSitter() != null && r.getSitter().getId().equals(id) && r.getStatus() == com.dhbw.pawsitters.model.sitting.SittingRequest.RequestStatus.COMPLETED)
                .count();

        List<com.dhbw.pawsitters.model.rating.Rating> ratings = ratingService.getRatingsForUser(id);

        return UserProfileResponse.fromUser(user, openRequests, sitsCompleted, ratings);
    }

    @PostMapping
    public AppUser createUser(@RequestBody AppUser user) {
        return userService.register(user);
    }
    @CrossOrigin(origins = "*")
    @PutMapping("/{id}/address")
    public AppUser updateAddress(@PathVariable Long id, @RequestBody AppUser user) {
        return userService.updateProfile(id, user);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{id}/profile")
    public AppUser updateProfile(@PathVariable Long id, @RequestBody AppUser user) {
        return userService.updateProfile(id, user);
    }

    @CrossOrigin(origins = "*")
    @PutMapping("/{id}/email")
    public AppUser updateEmail(@PathVariable Long id, @RequestBody String email) {
        return userService.updateEmail(id, email);
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/{id}/password")
    public void updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest req) {
        userService.updatePassword(id, req.oldPassword, req.newPassword);
    }

    public static class PasswordUpdateRequest {
        public String oldPassword;
        public String newPassword;
    }

}
