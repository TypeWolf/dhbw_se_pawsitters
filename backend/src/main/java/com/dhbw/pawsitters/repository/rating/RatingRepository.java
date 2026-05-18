package com.dhbw.pawsitters.repository.rating;

import com.dhbw.pawsitters.model.rating.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByRatedUserId(Long ratedUserId);
    List<Rating> findByRaterId(Long raterId);
    boolean existsBySittingRequestId(Long sittingRequestId);
}
