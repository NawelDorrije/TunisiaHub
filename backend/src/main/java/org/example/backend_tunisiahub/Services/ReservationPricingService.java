package org.example.backend_tunisiahub.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ReservationPricingService {

    private final BigDecimal bookingFeeRate;

    public ReservationPricingService(@Value("${booking.fee.rate:0.10}") BigDecimal bookingFeeRate) {
        this.bookingFeeRate = bookingFeeRate;
    }

    public ReservationQuote calculateQuote(BigDecimal pricePerSeat, int seatsRequested) {
        int normalizedSeats = Math.max(1, seatsRequested);
        BigDecimal normalizedPrice = pricePerSeat == null
                ? BigDecimal.ZERO
                : pricePerSeat.max(BigDecimal.ZERO);

        BigDecimal driverAmount = normalizedPrice
                .multiply(BigDecimal.valueOf(normalizedSeats))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = driverAmount
                .multiply(bookingFeeRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = driverAmount
                .add(serviceFee)
                .setScale(2, RoundingMode.HALF_UP);

        return new ReservationQuote(normalizedSeats, driverAmount, serviceFee, totalAmount);
    }
}
