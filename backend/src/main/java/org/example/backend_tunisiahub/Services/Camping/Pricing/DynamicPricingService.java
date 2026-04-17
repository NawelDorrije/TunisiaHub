package org.example.backend_tunisiahub.Services.Camping.Pricing;

import org.example.backend_tunisiahub.Entities.Camping.PricingContext;
import org.example.backend_tunisiahub.Entities.Camping.PricingResult;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DYNAMIC PRICING SERVICE — orchestrates the full pipeline
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Pipeline per spot:
 *   1. PricingSignalService  → gather weather + occupancy + demand signals
 *   2. GroqAIPricingEngine   → ask Llama3 for a multiplier (JSON response)
 *   3. Guardrails            → clamp to [basePrice×0.70, basePrice×2.50]
 *   4. Persist               → save dynamicPrice + lastPricedAt on Spot
 *   5. PricingAuditService   → async audit log
 *
 * Guardrails:
 *   floor   = basePrice × 0.70  (never drop below 70% of base)
 *   ceiling = basePrice × 2.50  (never exceed 2.5× base)
 *
 * Scheduling:
 *   Nightly cron at 03:00 AM Africa/Tunis (configurable)
 *   Can also be triggered on-demand via REST or programmatically.
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class DynamicPricingService {


    @Autowired private SpotRepository        spotRepository;
    @Autowired private PricingSignalService  signalService;
    @Autowired private GroqAIPricingEngine   groqEngine;
    @Autowired private PricingAuditService   auditService;

    // ── Scheduled nightly batch ────────────────────────────────────────────

    /**
     * Reprices every active spot every night at 03:00 AM.
     * Uses today as the check-in date proxy for occupancy/demand signals.
     *
     * Requires @EnableScheduling on the main application class.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Tunis")
    public void runNightlyRepricing() {
        System.out.println("[Pricing] Nightly repricing started at " + LocalDateTime.now());

        List<Spot> activeSpots = spotRepository.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .toList();

        int success = 0;
        int failed  = 0;

        for (Spot spot : activeSpots) {
            try {
                PricingResult result = computePrice(spot, LocalDate.now());
                applyAndSave(spot, result);
                success++;
            } catch (Exception e) {
                failed++;
                System.err.printf("[Pricing] Spot %d failed: %s%n",
                        spot.getId(), e.getMessage());
            }
        }

        System.out.printf("[Pricing] Done — %d updated, %d failed, total %d%n",
                success, failed, activeSpots.size());
    }

    // ── On-demand single-spot reprice ──────────────────────────────────────

    /**
     * Reprice one specific spot for a given check-in date.
     * Call this when: a booking is cancelled, a new spot is created,
     * or the owner manually triggers a refresh.
     */
    public PricingResult repriceSpot(Long spotId, LocalDate checkInDate) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found: " + spotId));

        PricingResult result = computePrice(spot, checkInDate);
        applyAndSave(spot, result);
        return result;
    }

    /**
     * Returns the effective price for a spot on a given date WITHOUT saving.
     *
     * Used in the booking flow to show the customer the real price before
     * they confirm. Returns the cached dynamicPrice if priced today;
     * computes on-the-fly (unsaved) otherwise.
     */
    public BigDecimal getEffectivePrice(Spot spot, LocalDate checkInDate) {
        // Return today's cached price if already computed
        if (spot.getDynamicPrice() != null
                && spot.getLastPricedAt() != null
                && spot.getLastPricedAt().toLocalDate().equals(LocalDate.now())) {
            return spot.getDynamicPrice();
        }
        // Compute on-the-fly (not persisted — fast path for booking widget)
        return computePrice(spot, checkInDate).dynamicPrice();
    }

    // ── Core pipeline ──────────────────────────────────────────────────────

    private PricingResult computePrice(Spot spot, LocalDate checkInDate) {

        // Step 1 — Gather signals
        PricingContext ctx = signalService.buildContext(spot, checkInDate);

        // Step 2 — Ask Groq/Llama3 for a raw multiplier
        double rawMultiplier = groqEngine.getMultiplier(ctx);

        // Step 3 — Compute AI-suggested price
        BigDecimal aiPrice = ctx.basePrice()
                .multiply(BigDecimal.valueOf(rawMultiplier))
                .setScale(2, RoundingMode.HALF_UP);

        // Step 4 — Apply per-spot constraints (min = basePrice, max = owner-defined)
        BigDecimal basePrice = ctx.basePrice();
        BigDecimal maxPrice  = spot.getMaxPrice();   // null means no upper limit

        BigDecimal finalPrice;
        String reason;

        if (aiPrice.compareTo(basePrice) < 0) {
            // AI went below base — floor to basePrice
            finalPrice = basePrice;
            reason = "Raised to base price (minimum allowed)";

        } else if (maxPrice != null && aiPrice.compareTo(maxPrice) > 0) {
            // AI exceeded owner's ceiling — cap to maxPrice
            finalPrice = maxPrice;
            reason = "Limited by owner max price";

        } else {
            // AI price is within bounds
            finalPrice = aiPrice;
            reason = "Price determined by AI";
        }

        System.out.printf(
                "[Pricing] Spot %d → base=%.2f × %.2f = AI %.2f → final %.2f (%s)%n",
                spot.getId(), basePrice.doubleValue(),
                rawMultiplier, aiPrice.doubleValue(),
                finalPrice.doubleValue(), reason);

        return new PricingResult(finalPrice, rawMultiplier, reason);
    }

    private void applyAndSave(Spot spot, PricingResult result) {
        spot.setDynamicPrice(result.dynamicPrice());
        spot.setLastPricedAt(LocalDateTime.now());
        spotRepository.save(spot);

        // Async audit log (includes full signal context)
        PricingContext ctx = signalService.buildContext(spot,
                spot.getLastPricedAt().toLocalDate());
        auditService.log(spot.getId(), result, ctx);
    }

    // ── Human-readable reason ──────────────────────────────────────────────

    private String buildReason(PricingContext ctx, double multiplier) {
        if (multiplier >= 1.40) return "Peak demand — high occupancy + local event";
        if (multiplier >= 1.20) return "High demand — weekend or seasonal pressure";
        if (multiplier >= 1.05) return "Moderate demand boost";
        if (multiplier <= 0.80) return "Low occupancy — discount to fill spot";
        if (multiplier <= 0.92) return "Slight discount — below average demand";
        return "Standard pricing — balanced signals";
    }
}