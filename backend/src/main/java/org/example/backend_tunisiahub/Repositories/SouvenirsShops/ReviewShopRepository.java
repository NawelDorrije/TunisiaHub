package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewShopRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserId(Long userId);

    List<Review> findByReviewTypeAndTargetId(ReviewType reviewType, Long targetId);
}
