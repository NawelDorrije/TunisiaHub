package org.example.backend_tunisiahub.Repositories.Event;

import org.example.backend_tunisiahub.Entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewEventRepository extends JpaRepository<Review, Long> {
}
