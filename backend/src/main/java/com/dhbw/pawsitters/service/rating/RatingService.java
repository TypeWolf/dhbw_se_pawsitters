package com.dhbw.pawsitters.service.rating;

import com.dhbw.pawsitters.model.rating.Rating;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {

    @Autowired
    private UnitOfWork unitOfWork;

    public Rating createRating(Long requestId, int stars, String comment, AppUser rater) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);

        if (request.getStatus() != SittingRequest.RequestStatus.COMPLETED) {
            throw new RuntimeException("Can only rate completed requests");
        }

        // Check if already rated
        List<Rating> existing = unitOfWork.getByProperty(Rating.class, "sittingRequest", request);
        if (!existing.isEmpty()) {
            throw new RuntimeException("Request already rated");
        }

        AppUser ratedUser;
        if (rater.getId().equals(request.getRequester().getId())) {
            // Owner is rating the sitter
            ratedUser = request.getSitter();
            if (ratedUser == null) {
                throw new RuntimeException("No sitter assigned to this request");
            }
        } else if (request.getSitter() != null && rater.getId().equals(request.getSitter().getId())) {
            // Sitter is rating the owner
            ratedUser = request.getRequester();
        } else {
            throw new RuntimeException("Only the requester or the sitter can rate this request");
        }

        if (stars < 1 || stars > 5) {
            throw new RuntimeException("Stars must be between 1 and 5");
        }

        Rating rating = Rating.builder()
                .sittingRequest(request)
                .rater(rater)
                .ratedUser(ratedUser)
                .stars(stars)
                .comment(comment)
                .build();

        return unitOfWork.save(rating);
    }

    public List<Rating> getRatingsForUser(Long userId) {
        AppUser user = unitOfWork.getById(AppUser.class, userId);
        return getRatingsForUser(user);
    }

    public List<Rating> getRatingsForUser(AppUser user) {
        return unitOfWork.getRatingsForUser(user);
    }

    public double getAverageRating(Long userId) {
        AppUser user = unitOfWork.getById(AppUser.class, userId);
        return getAverageRating(user);
    }

    public double getAverageRating(AppUser user) {
        List<Rating> ratings = getRatingsForUser(user);
        if (ratings.isEmpty()) {
            return 0.0;
        }
        return ratings.stream()
                .mapToInt(Rating::getStars)
                .average()
                .orElse(0.0);
    }

    public void populateAverageRating(AppUser user) {
        if (user == null) return;
        user.setAverageRating(getAverageRating(user));
    }
}
