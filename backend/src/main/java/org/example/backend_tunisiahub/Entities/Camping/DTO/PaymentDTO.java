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

    // Read-only
    Long id;
    BigDecimal amount;
    PaymentStatus status;
    String transactionRef;
    LocalDateTime createdAt;
    LocalDateTime paidAt;

    // Input
    @NotNull(message = "reservationId is required")
    Long reservationId;

    @NotNull(message = "Payment method is required")
    PaymentMethod method;

    // Read-only (response)
    String reservationSummary;
    Long  clientId;       // already there


}