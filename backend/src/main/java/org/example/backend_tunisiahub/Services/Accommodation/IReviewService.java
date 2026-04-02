package org.example.backend_tunisiahub.Services.Accommodation;

import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import java.util.List;

public interface IReviewService {

    List<AccommodationReview> retrieveAllReviews();

    AccommodationReview retrieveReview(Long reviewId);

    AccommodationReview addReview(Long accommodationId, AccommodationReview accommodationReview);

    void removeReview(Long reviewId);

    AccommodationReview modifyReview(Long id, AccommodationReview accommodationReview);

    List<AccommodationReview> getReviewsByAccommodation(Long accommodationId);
}
