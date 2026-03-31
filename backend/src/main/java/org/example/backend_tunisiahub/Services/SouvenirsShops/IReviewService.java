package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

public interface IReviewService {

    List<Review> retrieveAllReviews();

    Review retrieveReview(Long id);

    List<Review> retrieveReviewsByUser(Long userId);

    List<Review> retrieveReviewsByShop(Long shopId);

    List<Review> retrieveReviewsByProduct(Long productId);

    Review addReview(Review review);

    void deleteReview(Long id);

    Review modifyReview(Review review);
}
