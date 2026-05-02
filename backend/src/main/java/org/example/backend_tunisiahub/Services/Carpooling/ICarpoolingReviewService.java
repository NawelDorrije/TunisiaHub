package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Review;

import java.util.List;

public interface ICarpoolingReviewService {

    List<Review> retrieveDriverReviews(Long driverId);

    DriverReviewSummary retrieveDriverReviewSummary(Long driverId);

    Review retrieveReservationReview(Long reservationId);

    Review addReview(Long reservationId, Review review, Long currentUserId);

    Review modifyReview(Long reviewId, Review review, Long currentUserId);
}
