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
     * Spend funds for a booking. Draws from ownerCredit first, then sitterEarnings.
     * Returns the split so a later refund can restore each bucket exactly.
     */
    @Transactional
    public Split debit(Long userId, BigDecimal amount) {
        Wallet w = getOrCreate(userId);
        BigDecimal available = w.getOwnerCredit().add(w.getSitterEarnings());
        if (available.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds (available €"
                    + available.toPlainString() + ", needed €"
                    + amount.toPlainString() + ")");
        }

        BigDecimal fromCredit = w.getOwnerCredit().min(amount);
        BigDecimal fromEarnings = amount.subtract(fromCredit);

        w.setOwnerCredit(w.getOwnerCredit().subtract(fromCredit));
        w.setSitterEarnings(w.getSitterEarnings().subtract(fromEarnings));
        unitOfWork.save(w);

        return new Split(fromCredit, fromEarnings);
    }

    @Transactional
    public Wallet creditEarnings(Long userId, BigDecimal amount) {
        Wallet w = getOrCreate(userId);
        w.setSitterEarnings(w.getSitterEarnings().add(amount));
        return unitOfWork.save(w);
    }

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

    public record Split(BigDecimal fromOwnerCredit, BigDecimal fromSitterEarnings) {}
}
