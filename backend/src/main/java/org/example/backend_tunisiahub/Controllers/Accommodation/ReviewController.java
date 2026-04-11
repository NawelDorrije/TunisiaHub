package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import org.example.backend_tunisiahub.Services.Accommodation.IReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @GetMapping("/getAll")
    public ResponseEntity<List<AccommodationReview>> getAllReviews() {
        List<AccommodationReview> accommodationReviews = reviewService.retrieveAllReviews();
        return ResponseEntity.ok(accommodationReviews);
    }
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<List<AccommodationReview>> getReviewsByAccommodation(@PathVariable Long accommodationId) {
        if (accommodationId <= 0) return ResponseEntity.badRequest().build();
        List<AccommodationReview> reviews = reviewService.getReviewsByAccommodation(accommodationId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid review ID");
        AccommodationReview accommodationReview = reviewService.retrieveReview(id);
        if (accommodationReview == null) return ResponseEntity.status(404).body("Review not found with id: " + id);
        return ResponseEntity.ok(accommodationReview);
    }

    @PostMapping("/add/{accommodationId}")
    public ResponseEntity<?> createReview(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationReview review,
            @AuthenticationPrincipal String email) {

        if (accommodationId <= 0)
            return ResponseEntity.badRequest().body("Invalid accommodation ID");
        if (review.getRating() < 1 || review.getRating() > 5)
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
        if (review.getComment() == null || review.getComment().isEmpty())
            return ResponseEntity.badRequest().body("Comment is required");

        AccommodationReview saved = reviewService.addReview(
                accommodationId, review, email);

        if (saved == null)
            return ResponseEntity.status(400)
                    .body("⚠️ Your review contains inappropriate or offensive content. Please keep it respectful and try again.");

        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateReview(@PathVariable Long id, @RequestBody AccommodationReview accommodationReview) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid review ID");
        AccommodationReview existing = reviewService.retrieveReview(id);
        if (existing == null) return ResponseEntity.status(404).body("Review not found with id: " + id);
        AccommodationReview updated = reviewService.modifyReview(id, accommodationReview);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid review ID");
        AccommodationReview existing = reviewService.retrieveReview(id);
        if (existing == null) return ResponseEntity.status(404).body("Review not found with id: " + id);
        reviewService.removeReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }


}