package org.example.backend_tunisiahub.Controllers.Restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSearchRequest(
        @NotBlank String query,
        Optional<Double> latitude,
        Optional<Double> longitude
) {
    public AiSearchRequest {
        latitude = latitude == null ? Optional.empty() : latitude;
        longitude = longitude == null ? Optional.empty() : longitude;
    }
}
