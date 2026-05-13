package com.dhbw.pawsitters.service.wallet;

import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.model.wallet.Wallet;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class WalletServiceTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UnitOfWork unitOfWork;

    private AppUser user;

    @BeforeEach
    void setUp() {
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.rating.Rating.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.payment.Payment.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.sitting.SittingRequest.class);
        unitOfWork.deleteAll(com.dhbw.pawsitters.model.pet.Pet.class);
        unitOfWork.deleteAll(Wallet.class);
        unitOfWork.deleteAll(AppUser.class);

        user = unitOfWork.save(AppUser.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@test.com")
                .password("hash")
                .build());
    }

    @Test
    void testGetOrCreateNewWallet() {
        Wallet wallet = walletService.getOrCreate(user.getId());
        assertNotNull(wallet);
        assertEquals(WalletService.SIGNUP_BONUS, wallet.getOwnerCredit());
        assertEquals(WalletService.INITIAL_EARNINGS, wallet.getSitterEarnings());
    }

    @Test
    void testDebitDrawsFromCreditFirst() {
        BigDecimal amount = new BigDecimal("20.00");
        WalletService.Split split = walletService.debit(user.getId(), amount);

        assertEquals(0, new BigDecimal("20.00").compareTo(split.fromOwnerCredit()));
        assertEquals(0, BigDecimal.ZERO.compareTo(split.fromSitterEarnings()));

        Wallet wallet = walletService.getOrCreate(user.getId());
        assertEquals(0, new BigDecimal("30.00").compareTo(wallet.getOwnerCredit()));
    }

    @Test
    void testDebitDrawsFromEarningsIfCreditInsufficient() {
        // First, add some earnings
        walletService.creditEarnings(user.getId(), new BigDecimal("10.00"));
        
        // Bonus is 50.00, Total 60.00. Debit 55.00
        BigDecimal amount = new BigDecimal("55.00");
        WalletService.Split split = walletService.debit(user.getId(), amount);

        assertEquals(0, new BigDecimal("50.00").compareTo(split.fromOwnerCredit()));
        assertEquals(0, new BigDecimal("5.00").compareTo(split.fromSitterEarnings()));

        Wallet wallet = walletService.getOrCreate(user.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(wallet.getOwnerCredit()));
        assertEquals(0, new BigDecimal("5.00").compareTo(wallet.getSitterEarnings()));
    }

    @Test
    void testDebitInsufficientFundsThrows() {
        BigDecimal amount = new BigDecimal("100.00");
        assertThrows(RuntimeException.class, () -> walletService.debit(user.getId(), amount));
    }

    @Test
    void testWithdrawEarnings() {
        walletService.creditEarnings(user.getId(), new BigDecimal("25.00"));
        BigDecimal withdrawn = walletService.withdrawEarnings(user.getId());
        
        assertEquals(new BigDecimal("25.00"), withdrawn);
        Wallet wallet = walletService.getOrCreate(user.getId());
        assertEquals(BigDecimal.ZERO.setScale(2), wallet.getSitterEarnings());
    }
}
