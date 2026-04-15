package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.ReviewEligibilityResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

public interface IReviewShopService {

    Review retrieveReview(Long id);

    List<Review> retrieveReviewsByShop(Long shopId);

    List<Review> retrieveReviewsByProduct(Long productId);

    Review addShopReview(Long shopId, Integer rating, String comment);

    Review addProductReview(Long productId, Integer rating, String comment);

    void deleteReview(Long id);

    Review modifyReview(Long reviewId, Integer rating, String comment);

    ReviewEligibilityResponse getReviewsWithEligibilityForShop(Long shopId);

    ReviewEligibilityResponse getReviewsWithEligibilityForProduct(Long productId);
}
