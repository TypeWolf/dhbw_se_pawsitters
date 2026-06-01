package com.dhbw.pawsitters.controller.user;

import com.dhbw.pawsitters.model.rating.Rating;
import com.dhbw.pawsitters.model.user.AppUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private LocalDateTime createdAt;
    private Double averageRating;
    
    private long openRequestsCount;
    private long sitsCompletedCount;
    private List<Rating> ratings;

    public static UserProfileResponse fromUser(AppUser user, long openRequests, long sitsCompleted, List<Rating> ratings) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .city(user.getCity())
                .createdAt(user.getCreatedAt())
                .averageRating(user.getAverageRating())
                .openRequestsCount(openRequests)
                .sitsCompletedCount(sitsCompleted)
                .ratings(ratings)
                .build();
    }
}
