package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;

    @Override
    public List<Review> retrieveAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Review retrieveReview(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public List<Review> retrieveReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public List<Review> retrieveReviewsByShop(Long shopId) {
        return reviewRepository.findByReviewTypeAndTargetId(ReviewType.SHOP, shopId);
    }

    @Override
    public List<Review> retrieveReviewsByProduct(Long productId) {
        return reviewRepository.findByReviewTypeAndTargetId(ReviewType.PRODUCT, productId);
    }

    @Override
    public Review addReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public Review modifyReview(Review review) {
        return reviewRepository.save(review);
    }
}
