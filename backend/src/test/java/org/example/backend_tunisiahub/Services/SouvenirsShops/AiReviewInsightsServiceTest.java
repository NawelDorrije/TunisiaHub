package org.example.backend_tunisiahub.Services.SouvenirsShops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.OwnerReviewInsightsResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.junit.jupiter.api.Test;

class AiReviewInsightsServiceTest {

    @Test
    void fallbackInsightsSummarizeOwnerReviewsWithoutGeminiKey() {
        AiReviewInsightsService service = new AiReviewInsightsService(new ObjectMapper());

        User owner = new User();
        owner.setId(7L);
        owner.setPrenom("Sara");
        owner.setNom("Owner");
        owner.setEmail("owner@test.com");
        owner.setRole(RoleUser.OWNER);

        Shop shop = new Shop();
        shop.setId(10L);
        shop.setName("Medina Crafts");
        shop.setOwner(owner);

        Product product = new Product();
        product.setId(20L);
        product.setName("Ceramic Plate");
        product.setShop(shop);

        Review shopReview = review(1L, ReviewType.SHOP, shop, null, 2, "Slow delivery and poor packaging");
        Review productReview = review(2L, ReviewType.PRODUCT, null, product, 5, "Beautiful craftsmanship and friendly service");

        OwnerReviewInsightsResponse response = service.generateOwnerInsights(
                owner,
                List.of(shop),
                List.of(product),
                List.of(shopReview),
                List.of(productReview)
        );

        assertEquals(2, response.getTotalReviews());
        assertEquals(1, response.getShopReviewCount());
        assertEquals(1, response.getProductReviewCount());
        assertFalse(response.isGeneratedWithAi());
        assertTrue(response.getSummary().contains("2 active reviews"));
        assertTrue(response.getMainProblem().toLowerCase().contains("delivery")
                || response.getMainProblem().toLowerCase().contains("packaging"));
        assertFalse(response.getBestFeatures().isEmpty());
    }

    private Review review(Long id, ReviewType type, Shop shop, Product product, int rating, String comment) {
        Review review = new Review();
        review.setId(id);
        review.setReviewType(type);
        review.setTargetId(type == ReviewType.SHOP ? shop.getId() : product.getId());
        review.setShop(shop);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);
        review.setDeleted(false);
        return review;
    }
}
