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
    public Review addReview(Long userId, Long reservationId, String comment, int rating) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // Vérifier si la réservation est confirmée
        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new RuntimeException("You must confirm your reservation first");
        }

        Review review = new Review();

        review.setReservation(reservation);
        review.setComment(filterBadWords(comment));
        review.setRating(rating);
        review.setSentimentEmoji(analyzeSentiment(comment));
        review.setDate(new Date());
        return reviewRepository.save(review);
    }





    // ====================== Sentiment Analysis ======================
    private String analyzeSentiment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            return "😐";
        }

        String lower = comment.toLowerCase();

        if (lower.contains("excellent") || lower.contains("amazing") ||
                lower.contains("love") || lower.contains("perfect") ||
                lower.contains("awesome") || lower.contains("best")) {
            return "😍";
        }

        if (lower.contains("good") || lower.contains("great") ||
                lower.contains("nice") || lower.contains("fun")) {
            return "🙂";
        }

        if (lower.contains("bad") || lower.contains("terrible") ||
                lower.contains("worst") || lower.contains("hate") ||
                lower.contains("awful") || lower.contains("shit") ||
                lower.contains("fuck")) {
            return "😡";
        }

        return "🙂";
    }

    // ====================== Filtrage des mauvais mots ======================
    private String filterBadWords(String comment) {
        if (comment == null) return "";

        String[] badWords = {"fuck", "shit", "idiot", "stupid", "bitch", "asshole"};

        for (String word : badWords) {
            comment = comment.replaceAll("(?i)\\b" + word + "\\b", "****");
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