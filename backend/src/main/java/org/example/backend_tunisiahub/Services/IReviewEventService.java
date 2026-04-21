package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Review;

import java.util.List;

public interface IReviewEventService {

    List<Review> retrieveAllReviews();

    //Review retrieveReview(Long id);
    //List<ReviewResponseDTO> retrieveAllReviews();

    Review addReview(Long userId, Long reservationId, String comment, int rating);

    //ReviewResponseDTO addReview(ReviewRequestDTO dto);

    void deleteReview(Long id);

    Review modifyReview(Review review);
}