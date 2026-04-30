package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "ReservationPayment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    Reservation reservation;

    /** Full reservation total */
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    /**
     * The deposit (partial payment) collected online.
     * Must be >= (amount * minimumDepositPercent / 100).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal depositAmount;

    /**
     * Remaining balance to be collected at reception.
     * = amount - depositAmount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal remainingAmount;

    /**
     * Minimum deposit percentage required to confirm booking (e.g. 30 means 30%).
     * Stored so it can be audited later.
     */
    @Column(nullable = false)
    @Builder.Default
    Integer minimumDepositPercent = 30;

    /** Payment method used for the online deposit */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    PaymentMethod method;

    /**
     * Payment method chosen by the client for the remaining balance.
     * Null until the client selects it (CASH or CARD_AT_RECEPTION).
     */
    @Enumerated(EnumType.STRING)
    @Column
    PaymentMethod remainingPaymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;

    /** Unique transaction reference for the online deposit */
    @Column(unique = true)
    String transactionRef;

    @Column(nullable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime paidAt;

    /**
     * Base64-encoded PNG of the check-in QR code.
     * Generated after successful deposit payment and stored for resending.
     */
    @Column(columnDefinition = "TEXT")
    String qrCodeBase64;

    /**
     * Secure token embedded in the QR code to prevent forgery.
     */
    @Column(unique = true)
    String qrValidationToken;
}
