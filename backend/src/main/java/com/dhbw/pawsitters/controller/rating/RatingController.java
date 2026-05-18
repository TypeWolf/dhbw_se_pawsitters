package com.dhbw.pawsitters.controller.rating;

import com.dhbw.pawsitters.model.rating.Rating;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.rating.RatingService;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin(origins = "*")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private AppUserService userService;

    @PostMapping
    public Rating createRating(
            @RequestParam Long requestId,
            @RequestParam int stars,
            @RequestParam(required = false) String comment,
            @RequestParam Long raterId) {
        AppUser rater = userService.getUserById(raterId);
        return ratingService.createRating(requestId, stars, comment, rater);
    }

    @GetMapping("/user/{userId}")
    public List<Rating> getRatingsForUser(@PathVariable Long userId) {
        return ratingService.getRatingsForUser(userId);
    }

    @GetMapping("/user/{userId}/average")
    public double getAverageRating(@PathVariable Long userId) {
        return ratingService.getAverageRating(userId);
    }
}
