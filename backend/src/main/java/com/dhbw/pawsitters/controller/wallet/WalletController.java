package com.dhbw.pawsitters.controller.wallet;

import com.dhbw.pawsitters.model.payment.Payment;
import com.dhbw.pawsitters.model.wallet.Wallet;
import com.dhbw.pawsitters.service.payment.PaymentService;
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

    @GetMapping("/me")
    public WalletView getMine(@RequestParam Long userId) {
        Wallet wallet = walletService.getOrCreate(userId);
        List<Payment> history = paymentService.historyFor(userId);
        BigDecimal total = wallet.getOwnerCredit().add(wallet.getSitterEarnings());
        return new WalletView(
                wallet.getOwnerCredit(),
                wallet.getSitterEarnings(),
                total,
                history
        );
    }

    @PostMapping("/withdraw")
    public Payment withdraw(@RequestParam Long userId) {
        return paymentService.withdraw(userId);
    }

    public record WalletView(
            BigDecimal ownerCredit,
            BigDecimal sitterEarnings,
            BigDecimal total,
            List<Payment> history
    ) {}
}
