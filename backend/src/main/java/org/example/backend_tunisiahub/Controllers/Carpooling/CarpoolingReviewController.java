package org.example.backend_tunisiahub.Controllers.Carpooling;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Services.Carpooling.DriverReviewSummary;
import org.example.backend_tunisiahub.Services.Carpooling.ICarpoolingReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/carpooling-reviews")
@RequiredArgsConstructor
public class CarpoolingReviewController {

    private final ICarpoolingReviewService reviewService;

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReviewView>> getDriverReviews(@PathVariable Long driverId) {
        if (driverId == null || driverId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        List<ReviewView> reviews = reviewService.retrieveDriverReviews(driverId)
                .stream()
                .map(this::toView)
                .toList();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/driver-summary/{driverId}")
    public ResponseEntity<DriverReviewSummary> getDriverReviewSummary(@PathVariable Long driverId) {
        if (driverId == null || driverId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reviewService.retrieveDriverReviewSummary(driverId));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ReviewView> getReservationReview(@PathVariable Long reservationId) {
        if (reservationId == null || reservationId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Review review = reviewService.retrieveReservationReview(reservationId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toView(review));
    }

    @PostMapping("/add/{reservationId}")
    public ResponseEntity<ReviewView> addReview(@PathVariable Long reservationId,
                                                @RequestBody Review request,
                                                HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Review review = reviewService.addReview(reservationId, request, currentUserId);
        if (review == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(toView(review));
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<ReviewView> updateReview(@PathVariable Long reviewId,
                                                   @RequestBody Review request,
                                                   HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Review review = reviewService.modifyReview(reviewId, request, currentUserId);
        if (review == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(toView(review));
    }

    private ReviewView toView(Review review) {
        String reviewerName = "";
        if (review.getReservation() != null && review.getReservation().getReservedBy() != null) {
            String nom = review.getReservation().getReservedBy().getNom();
            String prenom = review.getReservation().getReservedBy().getPrenom();
            reviewerName = ((nom != null ? nom : "") + " " + (prenom != null ? prenom : "")).trim();
        }

        return new ReviewView(
                review.getId(),
                review.getComment(),
                review.getRating(),
                review.getDate(),
                review.getReservation() != null ? review.getReservation().getId() : null,
                reviewerName
        );
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String value = request.getHeader("X-USER-ID");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private record ReviewView(
            Long id,
            String comment,
            Integer rating,
            Date date,
            Long reservationId,
            String reviewerName
    ) {}
}
