package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/souvenir-shops/reviews")
@RequiredArgsConstructor
public class ReviewShopController {

    private final IReviewShopService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.retrieveAllReviews();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.retrieveReview(id);
    }

    @GetMapping("/user/{userId}")
    public List<Review> getReviewsByUser(@PathVariable Long userId) {
        return reviewService.retrieveReviewsByUser(userId);
    }

    @GetMapping("/shop/{shopId}")
    public List<Review> getReviewsByShop(@PathVariable Long shopId) {
        return reviewService.retrieveReviewsByShop(shopId);
    }

    @GetMapping("/product/{productId}")
    public List<Review> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.retrieveReviewsByProduct(productId);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return reviewService.modifyReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}
