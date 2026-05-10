package org.example.backend_tunisiahub.Repositories.Accommodation;

import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<AccommodationReview,Long> {
  List<AccommodationReview> findByAccommodationId(Long accommodationId);

  List<AccommodationReview> findByUserId(Long userId);

  List<AccommodationReview> findByRating(int rating);
  boolean existsByUserEmailAndAccommodationId(String email, Long accommodationId);
}
