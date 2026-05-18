package com.dhbw.pawsitters.service.payment;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.service.UnitOfWork;
import com.dhbw.pawsitters.service.wallet.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private WalletService walletService;

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
                .fromCard(split.fromCard())
                .status(Payment.Status.HELD)
                .createdAt(LocalDateTime.now())
                .build();
        return unitOfWork.save(p);
    }

    @Transactional
    public Payment release(Long paymentId) {
        Payment p = unitOfWork.getById(Payment.class, paymentId);
        if (p.getStatus() != Payment.Status.HELD) {
            throw new RuntimeException("Payment is not HELD");
        }
        SittingRequest r = unitOfWork.getById(SittingRequest.class, p.getSittingRequestId());
        if (r.getSitter() == null) {
            throw new RuntimeException("Cannot release: no sitter has accepted yet");
        }
        Long sitterId = r.getSitter().getId();
        walletService.creditEarnings(sitterId, p.getAmount());
        p.setPayeeId(sitterId);
        p.setStatus(Payment.Status.RELEASED);
        p.setResolvedAt(LocalDateTime.now());
        return unitOfWork.save(p);
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment p = unitOfWork.getById(Payment.class, paymentId);
        if (p.getStatus() != Payment.Status.HELD) {
            throw new RuntimeException("Payment is not HELD");
        }
        // Restore the wallet portions back to the user's balances.
        walletService.creditRefund(p.getPayerId(), p.getFromOwnerCredit(), p.getFromSitterEarnings());
        p.setStatus(Payment.Status.REFUNDED);
        p.setResolvedAt(LocalDateTime.now());
        unitOfWork.save(p);

        // If part of the hold was charged to the card, log a separate refund-to-card row.
        // The wallet does NOT get this amount back (matches how real Stripe refunds behave).
        BigDecimal cardPortion = p.getFromCard();
        if (cardPortion != null && cardPortion.signum() > 0) {
            LocalDateTime now = LocalDateTime.now();
            Payment cardRefund = Payment.builder()
                    .sittingRequestId(p.getSittingRequestId())
                    .payerId(p.getPayerId())
                    .amount(cardPortion)
                    .fromCard(cardPortion)
                    .status(Payment.Status.REFUNDED_TO_CARD)
                    .createdAt(now)
                    .resolvedAt(now)
                    .build();
            unitOfWork.save(cardRefund);
        }

        return p;
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
        return unitOfWork.save(p);
    }

    public Optional<Payment> findHeldForRequest(Long sittingRequestId) {
        return unitOfWork.getByProperties(Payment.class, Map.of(
                "sittingRequestId", sittingRequestId,
                "status", Payment.Status.HELD
        )).stream().findFirst();
    }

    public List<Payment> historyFor(Long userId) {
        String jpql = "SELECT p FROM Payment p WHERE p.payerId = :uid OR p.payeeId = :uid ORDER BY p.createdAt DESC";
        return unitOfWork.query(jpql, Payment.class, Map.of("uid", userId));
    }
}
