package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Repositories.Carpooling.CarpoolingReviewRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarpoolingReviewService implements ICarpoolingReviewService {

    private final CarpoolingReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<Review> retrieveDriverReviews(Long driverId) {
        return reviewRepository.findByDriverId(driverId);
    }

    @Override
    public DriverReviewSummary retrieveDriverReviewSummary(Long driverId) {
        Double averageRating = reviewRepository.findAverageRatingByDriverId(driverId);
        long reviewsCount = reviewRepository.countByReservation_Trip_Driver_Id(driverId);
        return new DriverReviewSummary(
                averageRating != null ? averageRating : 0,
                reviewsCount
        );
    }

    @Override
    public Review retrieveReservationReview(Long reservationId) {
        return reviewRepository.findByReservationId(reservationId);
    }

    @Override
    public Review addReview(Long reservationId, Review request, Long currentUserId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (!isValidReviewRequest(request, reservation, currentUserId)) {
            return null;
        }

        Review existingReview = reviewRepository.findByReservationId(reservationId);
        if (existingReview != null) {
            return null;
        }

        Review review = new Review();
        review.setComment(request.getComment().trim());
        review.setRating(request.getRating());
        review.setDate(new Date());
        review.setReservation(reservation);
        return reviewRepository.save(review);
    }

    @Override
    public Review modifyReview(Long reviewId, Review request, Long currentUserId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return null;
        }

        Reservation reservation = review.getReservation();
        if (!isValidReviewRequest(request, reservation, currentUserId)) {
            return null;
        }

        review.setComment(request.getComment().trim());
        review.setRating(request.getRating());
        review.setDate(new Date());
        return reviewRepository.save(review);
    }

    private boolean isValidReviewRequest(Review request, Reservation reservation, Long currentUserId) {
        if (request == null || reservation == null || currentUserId == null) {
            return false;
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            return false;
        }
        if (request.getComment() == null || request.getComment().isBlank()) {
            return false;
        }
        if (reservation.getType() != ReservationType.TripReservation) {
            return false;
        }
        if (reservation.getReservedBy() == null || !currentUserId.equals(reservation.getReservedBy().getId())) {
            return false;
        }
        return reservation.getTrip() != null && reservation.getTrip().getDriver() != null;
    }
}
