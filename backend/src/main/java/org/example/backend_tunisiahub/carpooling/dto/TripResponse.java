package org.example.backend_tunisiahub.carpooling.dto;

import org.example.backend_tunisiahub.carpooling.entity.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        Long driverId,
        String departurePoint,
        String destination,
        LocalDateTime departureDateTime,
        BigDecimal price,
        int seatsTotal,
        int seatsAvailable,
        TripStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
