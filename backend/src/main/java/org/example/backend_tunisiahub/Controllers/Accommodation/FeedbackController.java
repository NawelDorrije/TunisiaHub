package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Feedback;
import org.example.backend_tunisiahub.Services.Accommodation.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping("/add/{accommodationId}/{reservationId}")
    public ResponseEntity<?> addFeedback(
            @PathVariable Long accommodationId,
            @PathVariable Long reservationId,
            @RequestBody Feedback feedback,
            @AuthenticationPrincipal String email) {

        if (feedback.getRating() < 1 || feedback.getRating() > 5)
            return ResponseEntity.badRequest().body("Rating must be between 1 and 5");

        Feedback saved = feedbackService.addFeedback(
                accommodationId, reservationId, feedback, email);

        if (saved == null)
            return ResponseEntity.status(400)
                    .body("Feedback already submitted or invalid data.");

        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<List<Feedback>> getFeedback(
            @PathVariable Long accommodationId) {
        return ResponseEntity.ok(
                feedbackService.getFeedbackByAccommodation(accommodationId));
    }

    @GetMapping("/has-feedback/{reservationId}")
    public ResponseEntity<Boolean> hasFeedback(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(
                feedbackService.hasFeedback(reservationId));
    }
}