package com.dhbw.pawsitters.service.payment;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.pet.Pet;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.wallet.Wallet;
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
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser payer;
    private AppUser sitter;
    private SittingRequest request;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.rating.Rating.class);
        unitOfWork.deleteAll(Payment.class);
        unitOfWork.deleteAll(SittingRequest.class);
        unitOfWork.deleteAll(Pet.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.wallet.Wallet.class);
        unitOfWork.deleteAll(AppUser.class);

        payer = unitOfWork.save(AppUser.builder().firstName("P").lastName("P").email("p@t.com").password("h").build());
        sitter = unitOfWork.save(AppUser.builder().firstName("S").lastName("S").email("s@t.com").password("h").build());
        
        Pet pet = unitOfWork.save(Pet.builder().name("Dog").species("Dog").owner(payer).build());
        request = unitOfWork.save(SittingRequest.builder()
                .requester(payer)
                .pet(pet)
                .startTime(java.time.LocalDateTime.now().plusDays(1))
                .endTime(java.time.LocalDateTime.now().plusDays(1).plusHours(2))
                .status(SittingRequest.RequestStatus.ACCEPTED)
                .sitter(sitter)
                .build());
    }

    @Test
    void testHoldPayment() {
        Payment p = paymentService.hold(request.getId(), payer.getId(), new BigDecimal("10.00"));
        assertNotNull(p.getId());
        assertEquals(Payment.Status.HELD, p.getStatus());
    }

    @Test
    void testReleasePayment() {
        Payment held = paymentService.hold(request.getId(), payer.getId(), new BigDecimal("10.00"));
        Payment released = paymentService.release(held.getId());

        assertEquals(Payment.Status.RELEASED, released.getStatus());
        assertEquals(sitter.getId(), released.getPayeeId());
    }

    @Test
    void testRefundPayment() {
        Payment held = paymentService.hold(request.getId(), payer.getId(), new BigDecimal("10.00"));
        Payment refunded = paymentService.refund(held.getId());

        assertEquals(Payment.Status.REFUNDED, refunded.getStatus());
    }

    @Test
    void testPaymentHistory() {
        paymentService.hold(request.getId(), payer.getId(), new BigDecimal("5.00"));
        List<Payment> history = paymentService.historyFor(payer.getId());
        assertEquals(1, history.size());
    }
}
