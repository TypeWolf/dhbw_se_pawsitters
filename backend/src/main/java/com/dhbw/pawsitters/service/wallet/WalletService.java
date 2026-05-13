package com.dhbw.pawsitters.service.wallet;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.wallet.Wallet;
import com.dhbw.pawsitters.service.UnitOfWork;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    public static final BigDecimal SIGNUP_BONUS = new BigDecimal("50.00");
    public static final BigDecimal INITIAL_EARNINGS = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    @Autowired
    private UnitOfWork unitOfWork;

    @Autowired
    private AppUserService userService;

    @Transactional
    public Wallet getOrCreate(Long userId) {
        return unitOfWork.getByProperty(Wallet.class, "user.id", userId).stream()
                .findFirst()
                .orElseGet(() -> {
                    AppUser user = userService.getUserById(userId);
                    Wallet w = Wallet.builder()
                            .user(user)
                            .ownerCredit(SIGNUP_BONUS)
                            .sitterEarnings(INITIAL_EARNINGS)
                            .build();
                    return unitOfWork.save(w);
                });
    }

    /**
     * Spend funds for a booking. Order of draw: ownerCredit → sitterEarnings → saved card.
     *
     * If the wallet alone can't cover the amount AND the user has a card on file,
     * the remainder is auto-charged to the card. The returned {@link Split} records
     * how the amount was distributed so a future refund can restore each source.
     *
     * Throws {@code "Insufficient funds"} only when wallet + card combined still aren't enough.
     */
    @Transactional
    public Split debit(Long userId, BigDecimal amount) {
        Wallet w = getOrCreate(userId);

        BigDecimal fromCredit   = w.getOwnerCredit().min(amount);
        BigDecimal remaining    = amount.subtract(fromCredit);
        BigDecimal fromEarnings = w.getSitterEarnings().min(remaining);
        remaining = remaining.subtract(fromEarnings);

        BigDecimal fromCard = ZERO;
        if (remaining.signum() > 0) {
            AppUser user = w.getUser();
            if (user != null && user.getCardLast4() != null && !user.getCardLast4().isBlank()) {
                fromCard = remaining;
                remaining = ZERO;
            }
        }

        if (remaining.signum() > 0) {
            throw new RuntimeException("Insufficient funds — please add a card or top up your wallet.");
        }

        w.setOwnerCredit(w.getOwnerCredit().subtract(fromCredit));
        w.setSitterEarnings(w.getSitterEarnings().subtract(fromEarnings));
        unitOfWork.save(w);

        return new Split(fromCredit, fromEarnings, fromCard);
    }

    @Transactional
    public Wallet creditEarnings(Long userId, BigDecimal amount) {
        Wallet w = getOrCreate(userId);
        w.setSitterEarnings(w.getSitterEarnings().add(amount));
        return unitOfWork.save(w);
    }

    /**
     * Restore the wallet portion of a refund. The card portion is NOT credited back here —
     * the caller logs a separate {@code REFUNDED_TO_CARD} payment row instead (matches how
     * real card refunds work).
     */
    @Transactional
    public Wallet creditRefund(Long userId, BigDecimal toOwnerCredit, BigDecimal toSitterEarnings) {
        Wallet w = getOrCreate(userId);
        if (toOwnerCredit != null && toOwnerCredit.signum() > 0) {
            w.setOwnerCredit(w.getOwnerCredit().add(toOwnerCredit));
        }
        if (toSitterEarnings != null && toSitterEarnings.signum() > 0) {
            w.setSitterEarnings(w.getSitterEarnings().add(toSitterEarnings));
        }
        return unitOfWork.save(w);
    }

    /** Zero out sitter earnings (mock withdrawal). Returns the amount withdrawn. */
    @Transactional
    public BigDecimal withdrawEarnings(Long userId) {
        Wallet w = getOrCreate(userId);
        BigDecimal amount = w.getSitterEarnings();
        if (amount.signum() <= 0) {
            throw new RuntimeException("Nothing to withdraw");
        }
        w.setSitterEarnings(BigDecimal.ZERO.setScale(2));
        unitOfWork.save(w);
        return amount;
    }

    // ---------- Saved card management ----------

    @Transactional
    public AppUser saveCard(Long userId, String cardholderName, String cardLast4, String cardExpiry) {
        if (cardLast4 == null || cardLast4.isBlank()) {
            throw new RuntimeException("Card number is required");
        }
        AppUser user = userService.getUserById(userId);
        user.setCardholderName(cardholderName);
        user.setCardLast4(cardLast4);
        user.setCardExpiry(cardExpiry);
        return unitOfWork.save(user);
    }

    @Transactional
    public AppUser removeCard(Long userId) {
        AppUser user = userService.getUserById(userId);
        user.setCardholderName(null);
        user.setCardLast4(null);
        user.setCardExpiry(null);
        return unitOfWork.save(user);
    }

    public record Split(BigDecimal fromOwnerCredit, BigDecimal fromSitterEarnings, BigDecimal fromCard) {}
}
