package org.example.backend_tunisiahub.Entities.Camping;

import java.math.BigDecimal;

/**
 * Output of the AI pricing engine.
 */
public record PricingResult(
        BigDecimal dynamicPrice,
        double rawMultiplier,
        String reason
) {}
