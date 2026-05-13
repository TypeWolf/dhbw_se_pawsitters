package com.dhbw.pawsitters.service.sitting;

import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class SittingRequestServiceTest {

    @Autowired
    private SittingRequestService requestService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser requester;
    private AppUser sitter;
    private Pet pet;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.payment.Payment.class);
        unitOfWork.deleteAll(SittingRequest.class);
        unitOfWork.deleteAll(Pet.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.wallet.Wallet.class);
        unitOfWork.deleteAll(AppUser.class);

        requester = unitOfWork.save(AppUser.builder()
                .firstName("Requester")
                .lastName("User")
                .email("req@test.com")
                .password("hash123456789")
                .build());

        sitter = unitOfWork.save(AppUser.builder()
                .firstName("Sitter")
                .lastName("User")
                .email("sitter@test.com")
                .password("hash123456789")
                .build());

        pet = unitOfWork.save(Pet.builder()
                .name("Doggo")
                .species("Dog")
                .owner(requester)
                .build());
    }

    private SittingRequest.SittingRequestBuilder baseRequest() {
        return SittingRequest.builder()
                .requester(requester)
                .pet(pet)
                .startTime(java.time.LocalDateTime.now().plusDays(1))
                .endTime(java.time.LocalDateTime.now().plusDays(1).plusHours(2));
    }

    @Test
    void testCreateRequest() {
        SittingRequest request = baseRequest()
                .priceOffered(new BigDecimal("20.00"))
                .build();

        SittingRequest saved = requestService.createRequest(request);
        assertEquals(SittingRequest.RequestStatus.PENDING, saved.getStatus());
        assertNotNull(saved.getId());
    }

    @Test
    void testAcceptRequest() {
        SittingRequest request = requestService.createRequest(baseRequest().build());

        SittingRequest accepted = requestService.acceptRequest(request.getId(), sitter.getId());
        assertEquals(SittingRequest.RequestStatus.ACCEPTED, accepted.getStatus());
        assertEquals(sitter.getId(), accepted.getSitter().getId());
    }

    @Test
    void testAcceptOwnRequestThrows() {
        SittingRequest request = requestService.createRequest(baseRequest().build());

        assertThrows(RuntimeException.class, () -> requestService.acceptRequest(request.getId(), requester.getId()));
    }

    @Test
    void testCancelRequest() {
        SittingRequest request = requestService.createRequest(baseRequest().build());

        SittingRequest cancelled = requestService.cancelRequest(request.getId(), requester.getId());
        assertEquals(SittingRequest.RequestStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testCancelByOtherThrows() {
        SittingRequest request = requestService.createRequest(baseRequest().build());

        assertThrows(RuntimeException.class, () -> requestService.cancelRequest(request.getId(), sitter.getId()));
    }

    @Test
    void testCompleteRequest() {
        SittingRequest request = requestService.createRequest(baseRequest().build());
        requestService.acceptRequest(request.getId(), sitter.getId());

        SittingRequest completed = requestService.completeRequest(request.getId(), requester.getId());
        assertEquals(SittingRequest.RequestStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void testCompleteUnacceptedRequestThrows() {
        SittingRequest request = requestService.createRequest(baseRequest().build());

        assertThrows(RuntimeException.class, () -> requestService.completeRequest(request.getId(), requester.getId()));
    }
}
