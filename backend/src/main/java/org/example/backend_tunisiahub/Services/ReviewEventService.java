package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Repositories.Event.ReviewEventRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewEventService implements IReviewEventService {

    private final ReviewEventRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<Review> retrieveAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public Review retrieveReview(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public Review addReview(Long userId, Long reservationId,
                            String comment, int rating) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // ✅ 1. vérifier user
        if (!r.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // ✅ 2. vérifier confirmation
        if (!"CONFIRMED".equals(r.getStatus())) {
            throw new RuntimeException("You must confirm reservation first");
        }

        // ✅ 3. vérifier event terminé
        if (r.getEvent().getEndDate().isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Event not finished yet");
        }

        // ✅ 4. vérifier déjà commenté
        if (r.getReview() != null) {
            throw new RuntimeException("Already reviewed");
        }

        // ✅ 5. filtrer mauvais mots
        comment = filterBadWords(comment);

        Review review = new Review();
        review.setComment(comment);
        review.setRating(rating);
        review.setDate(new Date());

        review.setUser(r.getUser());
        review.setEvent(r.getEvent());
        review.setReservation(r);

        r.setReview(review);

        return reservationRepository.save(r).getReview();
    }
    private String filterBadWords(String comment) {

        String[] badWords = {"fuck", "shit", "idiot", "stupid"};

        for (String word : badWords) {
            comment = comment.replaceAll("(?i)" + word, "****");
        }

        return comment;
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    @Override
    public Review modifyReview(Review review) {
        return reviewRepository.save(review);
    }
}