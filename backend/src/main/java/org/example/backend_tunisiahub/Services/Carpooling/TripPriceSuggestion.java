package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripPriceSuggestion {

    private BigDecimal suggestedPrice;

    private BigDecimal basePrice;

    private BigDecimal minHistoricalPrice;

    private BigDecimal maxHistoricalPrice;

    private Integer similarTripsCount;

    private Boolean holidayAdjusted;

    private String holidayName;

    private String message;
}
