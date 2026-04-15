package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.ReviewEligibilityResponse;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CreateReviewRequest;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.UpdateReviewRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Services.SouvenirsShops.IReviewShopService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/souvenir-shops/reviews", "/api/souvenir-shops/souvenir-shops/reviews"})
@RequiredArgsConstructor
public class ReviewShopController {

    private final IReviewShopService reviewService;

    @PostMapping("/shop/{shopId}")
    public Review createShopReview(@PathVariable Long shopId, @RequestBody CreateReviewRequest request) {
        return reviewService.addShopReview(
                shopId,
                request == null ? null : request.getRating(),
                request == null ? null : request.getComment()
        );
    }

    @PostMapping("/product/{productId}")
    public Review createProductReview(@PathVariable Long productId, @RequestBody CreateReviewRequest request) {
        return reviewService.addProductReview(
                productId,
                request == null ? null : request.getRating(),
                request == null ? null : request.getComment()
        );
    }

    @PutMapping("/{id}")
    public Review updateReview(@PathVariable Long id, @RequestBody UpdateReviewRequest request) {
        return reviewService.modifyReview(
                id,
                request == null ? null : request.getRating(),
                request == null ? null : request.getComment()
        );
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/shop/{shopId}")
    public List<Review> getReviewsByShop(@PathVariable Long shopId) {
        return reviewService.retrieveReviewsByShop(shopId);
    }

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.retrieveReviewsByProduct(productId);
    }

    @GetMapping("/shop/{shopId}/with-eligibility")
    public ReviewEligibilityResponse getReviewsWithEligibilityByShop(@PathVariable Long shopId) {
        return reviewService.getReviewsWithEligibilityForShop(shopId);
    }

    @GetMapping("/product/{productId}/with-eligibility")
    public ReviewEligibilityResponse getReviewsWithEligibilityByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsWithEligibilityForProduct(productId);
    }
}
