package org.example.backend_tunisiahub.Services.Camping.Pricing;

import org.example.backend_tunisiahub.Entities.Camping.PricingAudit;
import org.example.backend_tunisiahub.Entities.Camping.PricingContext;
import org.example.backend_tunisiahub.Entities.Camping.PricingResult;
import org.example.backend_tunisiahub.Repositories.Camping.PricingAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Asynchronously persists one audit record per pricing decision.
 * Runs on a separate thread so it never blocks the repricing loop.
 *
 * Requires @EnableAsync on the main application class.
 */
@Service
public class PricingAuditService {

    @Autowired private PricingAuditRepository auditRepository;

    /**
     * Save a full audit record including both the result and the signals
     * that produced it (needed for future ML retraining).
     */
    @Async
    public void log(Long spotId, PricingResult result, PricingContext ctx) {
        try {
            PricingAudit audit = new PricingAudit();
            audit.setSpotId(spotId);
            audit.setBasePrice(ctx.basePrice());
            audit.setDynamicPrice(result.dynamicPrice());
            audit.setRawMultiplier(result.rawMultiplier());
            audit.setClampedMultiplier(result.clampedMultiplier());
            audit.setReason(result.reason());

            // Signal snapshot
            audit.setWeatherScore(ctx.weatherScore());
            audit.setOccupancyRate(ctx.occupancyRate());
            audit.setDemandIndex(ctx.demandIndex());
            audit.setLocalEventNearby(ctx.localEventNearby());
            audit.setDayOfWeek(ctx.dayOfWeek().name());
            audit.setDaysUntilCheckIn(ctx.daysUntilCheckIn());

            auditRepository.save(audit);
        } catch (Exception e) {
            // Audit failure must never crash the main pricing flow
            System.err.println("[PricingAuditService] Failed to save audit: " + e.getMessage());
        }
    }

    /**
     * Fetch the last 10 pricing decisions for a spot.
     * Used by the owner dashboard to show price history.
     */
    public List<PricingAudit> getRecentAuditForSpot(Long spotId) {
        return auditRepository.findTop10BySpotIdOrderByCreatedAtDesc(spotId);
    }

    /**
     * Mark a booking as confirmed on the matching audit record.
     * Enables future supervised learning: price → conversion tracking.
     */
    @Async
    public void markBookingConfirmed(Long spotId) {
        List<PricingAudit> unconfirmed = auditRepository.findByBookingConfirmedFalse();
        unconfirmed.stream()
                .filter(a -> a.getSpotId().equals(spotId))
                .findFirst()
                .ifPresent(a -> {
                    a.setBookingConfirmed(true);
                    auditRepository.save(a);
                });
    }
}