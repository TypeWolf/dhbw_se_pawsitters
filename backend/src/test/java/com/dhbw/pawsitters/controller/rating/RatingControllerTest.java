package com.dhbw.pawsitters.controller.rating;

import com.dhbw.pawsitters.model.rating.Rating;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser owner;
    private AppUser sitter;
    private SittingRequest completedRequest;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(Rating.class);
        unitOfWork.deleteAll(SittingRequest.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.pet.Pet.class);
        unitOfWork.deleteAll(AppUser.class);

        owner = unitOfWork.save(AppUser.builder().firstName("Owner").lastName("L").email("owner@t.com").password("p").build());
        sitter = unitOfWork.save(AppUser.builder().firstName("Sitter").lastName("L").email("sitter@t.com").password("p").build());
        
        com.dhbw.pawsitters.model.pet.Pet pet = unitOfWork.save(com.dhbw.pawsitters.model.pet.Pet.builder()
                .name("Buddy")
                .species("Dog")
                .owner(owner)
                .build());

        completedRequest = unitOfWork.save(SittingRequest.builder()
                .requester(owner)
                .sitter(sitter)
                .pet(pet)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(2))
                .status(SittingRequest.RequestStatus.COMPLETED)
                .build());
    }

    @Test
    void testCreateRating() throws Exception {
        mockMvc.perform(post("/api/ratings")
                .param("requestId", completedRequest.getId().toString())
                .param("stars", "5")
                .param("comment", "Excellent!")
                .param("raterId", owner.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stars").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent!"));
    }

    @Test
    void testGetAverageRating() throws Exception {
        unitOfWork.save(Rating.builder()
                .sittingRequest(completedRequest)
                .rater(owner)
                .ratedUser(sitter)
                .stars(4)
                .build());

        mockMvc.perform(get("/api/ratings/user/" + sitter.getId() + "/average"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.0"));
    }

    @Test
    void testGetRatingsForUser() throws Exception {
        unitOfWork.save(Rating.builder()
                .sittingRequest(completedRequest)
                .rater(owner)
                .ratedUser(sitter)
                .stars(5)
                .comment("Great!")
                .build());

        mockMvc.perform(get("/api/ratings/user/" + sitter.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stars").value(5))
                .andExpect(jsonPath("$[0].comment").value("Great!"));
    }
}
