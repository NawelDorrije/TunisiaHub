package org.example.backend_tunisiahub.Controllers.Camping;

import org.example.backend_tunisiahub.Entities.Camping.PricingAudit;
import org.example.backend_tunisiahub.Entities.Camping.PricingResult;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.example.backend_tunisiahub.Services.Camping.Pricing.DynamicPricingService;
import org.example.backend_tunisiahub.Services.Camping.Pricing.PricingAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PRICING REST API
 * Base path: /api/pricing
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Endpoints:
 *
 *   GET  /api/pricing/spots/{spotId}/price?checkIn=YYYY-MM-DD
 *        → Real-time effective price for a spot on a check-in date.
 *          Called by the booking widget before the customer confirms.
 *
 *   POST /api/pricing/spots/{spotId}/reprice?checkIn=YYYY-MM-DD
 *        → Manually reprice one spot (owner or admin).
 *          Saves the result and returns full PricingResult.
 *
 *   POST /api/pricing/run
 *        → Admin trigger: run the full nightly repricing batch now.
 *
 *   GET  /api/pricing/spots/{spotId}/history
 *        → Last 10 pricing decisions for a spot (owner dashboard).
 *
 *   POST /api/pricing/spots/{spotId}/booking-confirmed
 *        → Mark the latest pricing audit as confirmed (booking happened).
 *          Feeds the future ML retraining feedback loop.
 * ═══════════════════════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    @Autowired private DynamicPricingService pricingService;
    @Autowired private PricingAuditService   auditService;
    @Autowired private SpotRepository        spotRepository;

    // ── 1. Get effective price (used in booking widget) ────────────────────

    /**
     * GET /api/pricing/spots/{spotId}/price?checkIn=2025-07-20
     *
     * Returns the current dynamic price for a spot on a given check-in date.
     * If the spot was already priced today, returns the cached value instantly.
     * Otherwise computes on-the-fly without saving.
     *
     * Example response:
     * {
     *   "spotId": 42,
     *   "checkIn": "2025-07-20",
     *   "basePrice": 80.00,
     *   "dynamicPrice": 104.00,
     *   "multiplier": 1.30,
     *   "pricingActive": true
     * }
     */
    @GetMapping("/spots/{spotId}/price")
    public ResponseEntity<Map<String, Object>> getEffectivePrice(
            @PathVariable Long spotId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn) {

        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new RuntimeException("Spot not found: " + spotId));

        BigDecimal effectivePrice = pricingService.getEffectivePrice(spot, checkIn);
        BigDecimal base = spot.getBasePrice();
        double multiplier = base.compareTo(BigDecimal.ZERO) > 0
                ? effectivePrice.divide(base, 4, java.math.RoundingMode.HALF_UP).doubleValue()
                : 1.0;

        return ResponseEntity.ok(Map.of(
                "spotId",        spotId,
                "checkIn",       checkIn.toString(),
                "basePrice",     base,
                "dynamicPrice",  effectivePrice,
                "multiplier",    Math.round(multiplier * 100.0) / 100.0,
                "pricingActive", spot.getDynamicPrice() != null
        ));
    }

    // ── 2. Manually reprice one spot ───────────────────────────────────────

    /**
     * POST /api/pricing/spots/{spotId}/reprice?checkIn=2025-07-20
     *
     * Triggers a full AI reprice for one spot, saves the result, and returns
     * the PricingResult. checkIn defaults to today if not provided.
     *
     * Use when: a booking is cancelled, a spot is newly created, or the owner
     * wants a fresh AI estimate immediately.
     */
    @PostMapping("/spots/{spotId}/reprice")
    public ResponseEntity<PricingResult> repriceSpot(
            @PathVariable Long spotId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn) {

        LocalDate date = (checkIn != null) ? checkIn : LocalDate.now();
        PricingResult result = pricingService.repriceSpot(spotId, date);
        return ResponseEntity.ok(result);
    }

    // ── 3. Run full batch repricing now ────────────────────────────────────

    /**
     * POST /api/pricing/run
     *
     * Admin endpoint: runs the same job as the nightly cron, immediately.
     * Useful for testing or forcing a price refresh after bulk data changes.
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> runFullReprice() {
        pricingService.runNightlyRepricing();
        return ResponseEntity.ok(Map.of(
                "status",    "success",
                "message",   "Full repricing batch triggered",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // ── 4. Pricing history for a spot ──────────────────────────────────────

    /**
     * GET /api/pricing/spots/{spotId}/history
     *
     * Returns the last 10 pricing decisions for a spot.
     * Used by the owner dashboard to show how and why prices changed.
     */
    @GetMapping("/spots/{spotId}/history")
    public ResponseEntity<List<PricingAudit>> getPricingHistory(
            @PathVariable Long spotId) {

        return ResponseEntity.ok(auditService.getRecentAuditForSpot(spotId));
    }

    // ── 5. Mark booking confirmed ──────────────────────────────────────────

    /**
     * POST /api/pricing/spots/{spotId}/booking-confirmed
     *
     * Called after a booking is successfully confirmed for this spot.
     * Updates the audit record so we can later train: "did the price convert?"
     * Call this from your existing reservation confirmation flow.
     */
    @PostMapping("/spots/{spotId}/booking-confirmed")
    public ResponseEntity<Map<String, String>> markConfirmed(
            @PathVariable Long spotId) {

        auditService.markBookingConfirmed(spotId);
        return ResponseEntity.ok(Map.of("status", "success"));
    }
    // ── 6. Get latest audit for a spot (for popup explanation) ──────────
    @GetMapping("/spots/{spotId}/latest-audit")
    public ResponseEntity<?> getLatestAudit(@PathVariable Long spotId) {
        return auditService.getRecentAuditForSpot(spotId)
                .stream()
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}