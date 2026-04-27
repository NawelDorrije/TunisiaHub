package org.example.backend_tunisiahub.Controllers.Restaurant.dto;

import java.util.List;

public record RestaurantRecommendationResponseDto(
        String reason,
        List<RestaurantRecommendationItemDto> restaurants
) {
}
