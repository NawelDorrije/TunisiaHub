package org.example.backend_tunisiahub.Services.Accommodation;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
<<<<<<< HEAD
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
=======
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Accommodation.ReviewRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
@Service
public class ReviewService implements IReviewService {

  final ReviewRepository reviewRepository;
  final AccommodationService accommodationService;
  final UserRepository userRepository;
  final RestTemplate restTemplate;

  @Value("${ai.service.url}")
  private String aiServiceUrl;

  public ReviewService(
    ReviewRepository reviewRepository,
    AccommodationService accommodationServiceImp,
    UserRepository userRepository,
    RestTemplate restTemplate) {
    this.reviewRepository = reviewRepository;
    this.accommodationService = accommodationServiceImp;
    this.userRepository = userRepository;
    this.restTemplate = restTemplate;
  }

  private boolean isReviewAppropriate(AccommodationReview review) {
    try {
      Map<String, Object> request = new HashMap<>();
      request.put("comment", review.getComment());
      request.put("rating", review.getRating());

      ResponseEntity<Map> response = restTemplate.postForEntity(
        aiServiceUrl + "/moderate-review",
        request,
        Map.class
      );

      Map<String, Object> body = response.getBody();
      if (body != null) {
        Object isAppropriate = body.get("is_appropriate");
        if (isAppropriate instanceof Boolean) {
          return (Boolean) isAppropriate;
        }
      }
      return true;
    } catch (Exception e) {
      System.out.println("Moderation error: " + e.getMessage());
      return true; // fail open
    }
  }

  @Override
  public AccommodationReview addReview(
    Long accommodationId,
    AccommodationReview review,
    String email) {

    // Check if user already reviewed this accommodation
    if (reviewRepository.existsByUserEmailAndAccommodationId(email, accommodationId)) {
      return null; // ← null signals already reviewed
    }

    if (!isReviewAppropriate(review)) {
      return null;
    }

    Accommodation accommodation = accommodationService
      .retrieveAccommodation(accommodationId);
    if (accommodation == null) return null;

    User user = userRepository.findByEmail(email);
    review.setAccommodation(accommodation);
    review.setUser(user);
    review.setReviewDate(LocalDate.now());
    return reviewRepository.save(review);
  }

  @Override
  public AccommodationReview modifyReview(Long id, AccommodationReview updated) {
    AccommodationReview existing = reviewRepository.findById(id).orElse(null);
    if (existing == null) return null;
    existing.setRating(updated.getRating());
    existing.setComment(updated.getComment());
    return reviewRepository.save(existing);
  }

  @Override
  public List<AccommodationReview> retrieveAllReviews() {
    return reviewRepository.findAll();
  }

  @Override
  public AccommodationReview retrieveReview(Long reviewId) {
    return reviewRepository.findById(reviewId).orElse(null);
  }

  @Override
  public void removeReview(Long reviewId) {
    reviewRepository.deleteById(reviewId);
  }

  @Override
  public List<AccommodationReview> getReviewsByAccommodation(Long accommodationId) {
    return reviewRepository.findByAccommodationId(accommodationId);
  }
>>>>>>> origin/feature/integrated-app-event
}
