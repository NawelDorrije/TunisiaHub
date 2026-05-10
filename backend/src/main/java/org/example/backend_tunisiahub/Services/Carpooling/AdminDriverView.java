package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.User.User;

import java.util.List;

public record AdminDriverView(
        User driver,
        List<Trip> trips,
        long reservationsCount,
        long canceledReservationsCount,
        double cancellationRate,
        double averageRating,
        long reviewsCount,
        long reportedIssues
) {
}
