package com.dhbw.pawsitters.service.rating;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.rating.Rating;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RatingServiceTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser owner;
    private AppUser sitter;
    private SittingRequest completedRequest;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(Rating.class);
        unitOfWork.deleteAll(SittingRequest.class);
        unitOfWork.deleteAll(Pet.class);
        unitOfWork.deleteAll(AppUser.class);

        owner = unitOfWork.save(AppUser.builder().firstName("Owner").lastName("L").email("owner@t.com").password("p").build());
        sitter = unitOfWork.save(AppUser.builder().firstName("Sitter").lastName("L").email("sitter@t.com").password("p").build());
        
        Pet pet = unitOfWork.save(Pet.builder().name("Dog").species("Dog").owner(owner).build());
        
        completedRequest = unitOfWork.save(SittingRequest.builder()
                .requester(owner)
                .sitter(sitter)
                .pet(pet)
                .startTime(java.time.LocalDateTime.now().minusDays(1))
                .endTime(java.time.LocalDateTime.now().minusDays(1).plusHours(2))
                .status(SittingRequest.RequestStatus.COMPLETED)
                .build());
    }

    @Test
    void testCreateRating() {
        Rating rating = ratingService.createRating(completedRequest.getId(), 5, "Great sitter!", owner);
        
        assertNotNull(rating.getId());
        assertEquals(5, rating.getStars());
        assertEquals("Great sitter!", rating.getComment());
        assertEquals(sitter.getId(), rating.getRatedUser().getId());
    }

    @Test
    void testCannotRateTwice() {
        ratingService.createRating(completedRequest.getId(), 5, "First", owner);
        
        assertThrows(RuntimeException.class, () -> 
            ratingService.createRating(completedRequest.getId(), 4, "Second", owner));
    }

    @Test
    void testOnlyCompletedCanBeRated() {
        SittingRequest pending = unitOfWork.save(SittingRequest.builder()
                .requester(owner)
                .status(SittingRequest.RequestStatus.PENDING)
                .pet(unitOfWork.getAll(Pet.class).get(0))
                .startTime(java.time.LocalDateTime.now())
                .endTime(java.time.LocalDateTime.now())
                .build());
        
        assertThrows(RuntimeException.class, () -> 
            ratingService.createRating(pending.getId(), 5, "Late", owner));
    }

    @Test
    void testAverageRating() {
        // Create another request and rate it
        Pet pet = unitOfWork.getAll(Pet.class).get(0);
        SittingRequest r2 = unitOfWork.save(SittingRequest.builder()
                .requester(owner).sitter(sitter).pet(pet)
                .status(SittingRequest.RequestStatus.COMPLETED)
                .startTime(java.time.LocalDateTime.now()).endTime(java.time.LocalDateTime.now())
                .build());
        
        ratingService.createRating(completedRequest.getId(), 5, "Good", owner);
        ratingService.createRating(r2.getId(), 3, "Okay", owner);
        
        double avg = ratingService.getAverageRating(sitter.getId());
        assertEquals(4.0, avg, 0.01);
    }
}
