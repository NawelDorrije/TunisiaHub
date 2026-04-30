package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerReviewInsightsResponse {
    private Long ownerId;
    private String ownerName;
    private int totalReviews;
    private int shopReviewCount;
    private int productReviewCount;
    private double averageShopRating;
    private double averageProductRating;
    private String summary;
    private String mainProblem;
    private String mostFrequentProblem;
    private List<String> bestFeatures;
    private List<Review> shopReviews;
    private List<Review> productReviews;
    private boolean generatedWithAi;
}
