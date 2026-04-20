package org.example.backend_tunisiahub.Services;

import java.math.BigDecimal;

public record ReservationQuote(
        int seatsRequested,
        BigDecimal driverAmount,
        BigDecimal serviceFee,
        BigDecimal totalAmount
) {
}
