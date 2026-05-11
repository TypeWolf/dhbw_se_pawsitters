package com.dhbw.pawsitters.model.wallet;

import com.dhbw.pawsitters.model.user.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private AppUser user;

    /** Non-withdrawable promo / signup credit. Spendable on bookings. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal ownerCredit;

    /** Withdrawable earnings from completed sittings. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sitterEarnings;
}
