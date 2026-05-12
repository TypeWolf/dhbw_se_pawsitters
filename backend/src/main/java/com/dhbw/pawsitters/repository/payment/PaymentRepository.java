package com.dhbw.pawsitters.repository.payment;

import com.dhbw.pawsitters.model.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySittingRequestId(Long sittingRequestId);
    Optional<Payment> findFirstBySittingRequestIdAndStatus(Long sittingRequestId, Payment.Status status);
    List<Payment> findByPayerIdOrPayeeIdOrderByCreatedAtDesc(Long payerId, Long payeeId);
}
