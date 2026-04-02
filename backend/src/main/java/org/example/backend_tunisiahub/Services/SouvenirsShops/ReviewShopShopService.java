package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewShopRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewShopShopService implements IReviewShopService {

    private final ReviewShopRepository reviewShopRepository;

    @Override
    public List<Review> retrieveAllReviews() {
        return reviewShopRepository.findAll();
    }

    @Override
    public Review retrieveReview(Long id) {
        return reviewShopRepository.findById(id).orElse(null);
    }

    @Override
    public List<Review> retrieveReviewsByUser(Long userId) {
        return reviewShopRepository.findByUserId(userId);
    }

    @Override
    public List<Review> retrieveReviewsByShop(Long shopId) {
        return reviewShopRepository.findByReviewTypeAndTargetId(ReviewType.SHOP, shopId);
    }

    @Override
    public List<Review> retrieveReviewsByProduct(Long productId) {
        return reviewShopRepository.findByReviewTypeAndTargetId(ReviewType.PRODUCT, productId);
    }

    @Override
    public Review addReview(Review review) {
        return reviewShopRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewShopRepository.deleteById(id);
    }

    @Override
    public Review modifyReview(Review review) {
        return reviewShopRepository.save(review);
    }
}
