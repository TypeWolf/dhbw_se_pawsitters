package com.dhbw.pawsitters.model.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nullable — withdrawals and refund-to-card rows are not tied to a sitting request. */
    private Long sittingRequestId;

    @Column(nullable = false)
    private Long payerId;

    private Long payeeId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** How much of the held amount came from the payer's owner credit. */
    @Column(precision = 12, scale = 2)
    private BigDecimal fromOwnerCredit;

    /** How much of the held amount came from the payer's sitter earnings. */
    @Column(precision = 12, scale = 2)
    private BigDecimal fromSitterEarnings;

    /** How much of the held amount was auto-charged to the payer's saved card. */
    @Column(precision = 12, scale = 2)
    private BigDecimal fromCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    public enum Status {
        HELD,
        RELEASED,
        REFUNDED,
        REFUNDED_TO_CARD,
        WITHDRAWN
    }
}
