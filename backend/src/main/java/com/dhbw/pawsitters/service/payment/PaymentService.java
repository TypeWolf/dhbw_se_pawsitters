package com.dhbw.pawsitters.service.payment;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.repository.payment.PaymentRepository;
import com.dhbw.pawsitters.repository.sitting.SittingRequestRepository;
import com.dhbw.pawsitters.service.wallet.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private SittingRequestRepository requestRepository;

    @Transactional
    public Payment hold(Long sittingRequestId, Long payerId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Hold amount must be positive");
        }
        WalletService.Split split = walletService.debit(payerId, amount);
        Payment p = Payment.builder()
                .sittingRequestId(sittingRequestId)
                .payerId(payerId)
                .amount(amount)
                .fromOwnerCredit(split.fromOwnerCredit())
                .fromSitterEarnings(split.fromSitterEarnings())
                .status(Payment.Status.HELD)
                .createdAt(LocalDateTime.now())
                .build();
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment release(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (p.getStatus() != Payment.Status.HELD) {
            throw new RuntimeException("Payment is not HELD");
        }
        SittingRequest r = requestRepository.findById(p.getSittingRequestId())
                .orElseThrow(() -> new RuntimeException("Sitting request not found"));
        if (r.getSitter() == null) {
            throw new RuntimeException("Cannot release: no sitter has accepted yet");
        }
        Long sitterId = r.getSitter().getId();
        walletService.creditEarnings(sitterId, p.getAmount());
        p.setPayeeId(sitterId);
        p.setStatus(Payment.Status.RELEASED);
        p.setResolvedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (p.getStatus() != Payment.Status.HELD) {
            throw new RuntimeException("Payment is not HELD");
        }
        walletService.creditRefund(p.getPayerId(), p.getFromOwnerCredit(), p.getFromSitterEarnings());
        p.setStatus(Payment.Status.REFUNDED);
        p.setResolvedAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment withdraw(Long userId) {
        BigDecimal amount = walletService.withdrawEarnings(userId);
        LocalDateTime now = LocalDateTime.now();
        Payment p = Payment.builder()
                .payerId(userId)
                .amount(amount)
                .status(Payment.Status.WITHDRAWN)
                .createdAt(now)
                .resolvedAt(now)
                .build();
        return paymentRepository.save(p);
    }

    public Optional<Payment> findHeldForRequest(Long sittingRequestId) {
        return paymentRepository.findFirstBySittingRequestIdAndStatus(sittingRequestId, Payment.Status.HELD);
    }

    public List<Payment> historyFor(Long userId) {
        return paymentRepository.findByPayerIdOrPayeeIdOrderByCreatedAtDesc(userId, userId);
    }
}
