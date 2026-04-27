package org.example.backend_tunisiahub.Controllers.Restaurant.dto;

public record RestaurantRecommendationItemDto(
        Long id,
        String name,
        String cuisine,
        Double rating,
        String image
) {
}
