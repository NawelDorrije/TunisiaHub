package org.example.backend_tunisiahub.Controllers.Restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSearchResult(
        @NotNull Long restaurantId,
        @Min(0) @Max(100) int matchScore,
        @NotBlank String reason,
        String suggestedTime,
        @PositiveOrZero int people
) {
}
