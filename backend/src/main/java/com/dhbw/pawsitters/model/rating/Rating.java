package com.dhbw.pawsitters.model.rating;

import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sitting_request_id", nullable = false)
    private SittingRequest sittingRequest;

    @ManyToOne
    @JoinColumn(name = "rater_id", nullable = false)
    private AppUser rater;

    @ManyToOne
    @JoinColumn(name = "rated_user_id", nullable = false)
    private AppUser ratedUser;

    @Column(nullable = false)
    private int stars; // 1-5

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
