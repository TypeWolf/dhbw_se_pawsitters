package com.dhbw.pawsitters.controller.wallet;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.wallet.Wallet;
import com.dhbw.pawsitters.service.payment.PaymentService;
import com.dhbw.pawsitters.service.user.AppUserService;
import com.dhbw.pawsitters.service.wallet.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AppUserService userService;

    @GetMapping("/me")
    public WalletView getMine(@RequestParam Long userId) {
        Wallet wallet = walletService.getOrCreate(userId);
        AppUser user = userService.getUserById(userId);
        List<Payment> history = paymentService.historyFor(userId);
        BigDecimal total = wallet.getOwnerCredit().add(wallet.getSitterEarnings());
        CardView card = (user.getCardLast4() != null && !user.getCardLast4().isBlank())
                ? new CardView(user.getCardLast4(), user.getCardExpiry())
                : null;
        return new WalletView(
                wallet.getOwnerCredit(),
                wallet.getSitterEarnings(),
                total,
                card,
                history
        );
    }

    @PostMapping("/withdraw")
    public Payment withdraw(@RequestParam Long userId) {
        return paymentService.withdraw(userId);
    }

    @PostMapping("/card")
    public CardView saveCard(@RequestParam Long userId, @RequestBody CardRequest body) {
        AppUser user = walletService.saveCard(
                userId,
                body.cardholderName(),
                body.last4(),
                body.expiry()
        );
        return new CardView(user.getCardLast4(), user.getCardExpiry());
    }

    @DeleteMapping("/card")
    public void deleteCard(@RequestParam Long userId) {
        walletService.removeCard(userId);
    }

    public record WalletView(
            BigDecimal ownerCredit,
            BigDecimal sitterEarnings,
            BigDecimal total,
            CardView card,
            List<Payment> history
    ) {}

    /** Response shape — only the safe fields. CVC + cardholder never leave the server. */
    public record CardView(String last4, String expiry) {}

    /** Request body. CVC is accepted-but-ignored (sent by the form for realism). */
    public record CardRequest(String cardholderName, String last4, String expiry, String cvc) {}
}
