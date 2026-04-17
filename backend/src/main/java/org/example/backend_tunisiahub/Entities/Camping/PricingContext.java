package org.example.backend_tunisiahub.Entities.Camping;


import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Immutable snapshot of all signals used to compute a dynamic price
 * for a single Spot at a given moment.
 */
public record PricingContext(

        Long spotId,
        Long campingId,
        BigDecimal basePrice,

        /** 0.0 = severe storm → 1.0 = perfect sunny camping weather */
        double weatherScore,

        /** Fraction of camping spots currently booked (0.0–1.0) */
        double occupancyRate,

        /**
         * Ratio of bookings this rolling-7-day window vs. same window last year.
         * 1.0 = same demand, 1.5 = 50% more demand.
         */
        double demandIndex,

        /** True when a festival/public-holiday falls within 50 km & 7 days */
        boolean localEventNearby,

        DayOfWeek dayOfWeek,

        /** How many days from now until the spot's check-in date */
        int daysUntilCheckIn

) {
    public boolean isWeekend() {
        return dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    public boolean isLastMinute() {
        return daysUntilCheckIn <= 3;
    }
}