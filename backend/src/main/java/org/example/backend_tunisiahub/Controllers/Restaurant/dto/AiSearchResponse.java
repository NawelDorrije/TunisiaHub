package org.example.backend_tunisiahub.Controllers.Restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSearchResponse(
        @NotBlank String message,
        @NotNull List<AiSearchResult> results,
        @Min(0) int totalMatches
) {
    public AiSearchResponse {
        results = results == null ? List.of() : List.copyOf(results);
    }
}
