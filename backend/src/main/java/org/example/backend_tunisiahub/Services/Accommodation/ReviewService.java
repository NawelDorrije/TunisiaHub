package org.example.backend_tunisiahub.Services.Accommodation;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import org.example.backend_tunisiahub.Repositories.Accommodation.ReviewRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    final ReviewRepository reviewRepository;
    final AccommodationService accommodationServiceImp;

    @Override
    public List<AccommodationReview> retrieveAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public AccommodationReview retrieveReview(Long reviewId) {
        return reviewRepository.findById(reviewId).get();
    }

    @Override
    public AccommodationReview addReview(Long accommodationId, AccommodationReview accommodationReview) {
        Accommodation accommodation = accommodationServiceImp.retrieveAccommodation(accommodationId);
        accommodationReview.setAccommodation(accommodation);
        accommodationReview.setReviewDate(LocalDate.now());
        return reviewRepository.save(accommodationReview);
    }

    @Override
    public void removeReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public AccommodationReview modifyReview(Long id, AccommodationReview updated) {
        AccommodationReview existing = reviewRepository.findById(id).get();
        existing.setRating(updated.getRating());
        existing.setComment(updated.getComment());
        return reviewRepository.save(existing);
    }

    @Override
    public List<AccommodationReview> getReviewsByAccommodation(Long accommodationId) {
        return reviewRepository.findByAccommodationId(accommodationId);
    }
}
