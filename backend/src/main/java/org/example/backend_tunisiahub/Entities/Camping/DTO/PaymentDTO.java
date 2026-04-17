package org.example.backend_tunisiahub.Entities.Camping.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Entities.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentDTO {

    // ── Read-only (response) ──────────────────────────────
    Long id;
    BigDecimal amount;          // total reservation price
    BigDecimal depositAmount;   // amount paid online now
    BigDecimal remainingAmount; // balance to pay at reception
    Integer minimumDepositPercent;
    PaymentStatus status;
    String transactionRef;
    LocalDateTime createdAt;
    LocalDateTime paidAt;

    /** Base64 PNG of the check-in QR code (returned after successful deposit) */
    String qrCodeBase64;

    // ── Input ─────────────────────────────────────────────
    @NotNull(message = "reservationId is required")
    Long reservationId;

    @NotNull(message = "Payment method is required")
    PaymentMethod method;

    /**
     * Deposit percentage the client wishes to pay (must be >= server minimum).
     * If null, the server uses the configured minimum (30%).
     */
    @DecimalMin(value = "0", message = "Deposit percent must be positive")
    @DecimalMax(value = "100", message = "Deposit percent cannot exceed 100")
    Integer depositPercent;

    /**
     * How the client intends to settle the remaining balance at reception.
     * Required when depositPercent < 100.
     */
    PaymentMethod remainingPaymentMethod;

    // ── Read-only enrichment ──────────────────────────────
    String reservationSummary;
    Long   clientId;
    String clientEmail;
    String clientName;
}