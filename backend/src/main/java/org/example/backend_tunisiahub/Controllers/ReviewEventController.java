package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Services.IReviewEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewEventController {

    private final IReviewEventService reviewService;

    @GetMapping("/all")
    public List<Review> getAll() {
        return reviewService.retrieveAllReviews();
    }

    @PostMapping("/add")
    public Review add(@RequestParam Long userId,
                      @RequestParam Long reservationId,
                      @RequestParam String comment,
                      @RequestParam int rating) {
        return reviewService.addReview(userId, reservationId, comment, rating);
    }


    @PutMapping("/update")
    public Review update(@RequestBody Review review) {
        return reviewService.modifyReview(review);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }


}