package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import org.example.backend_tunisiahub.Entities.SouvenirsShops.ShopCategory;

public record NearbyShopResponse(
        Long id,
        String name,
        String description,
        ShopCategory category,
        String city,
        String address,
        Double latitude,
        Double longitude,
        String photoUrl,
        Double averageRating,
        Double distanceKm
) {
}
