package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEligibilityResponse {
    private List<Review> reviews;
    private boolean canWriteReview;
    private Review userReview;
}