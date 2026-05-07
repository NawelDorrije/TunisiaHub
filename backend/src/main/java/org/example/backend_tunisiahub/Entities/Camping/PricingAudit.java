package org.example.backend_tunisiahub.Entities.Camping;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Audit record saved for every AI pricing decision.
 * Lets owners review why a price changed, and enables future ML retraining.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long spotId;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal basePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal dynamicPrice;

    @Column(nullable = false)
    double rawMultiplier;

    @Column(length = 500)
    String reason;

    // Signal snapshot for future ML retraining
    double weatherScore;
    double occupancyRate;
    double demandIndex;
    boolean localEventNearby;
    String dayOfWeek;
    int daysUntilCheckIn;

    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Set to true after a booking is confirmed for this spot.
     * Enables supervised learning: did this price convert?
     */
    Boolean bookingConfirmed = false;
}