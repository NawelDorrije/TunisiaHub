package org.example.backend_tunisiahub.Services.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.Feedback;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationReservationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.FeedbackRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    final FeedbackRepository feedbackRepository;
    final UserRepository userRepository;
    final AccommodationReservationRepository reservationRepository;
    final AccommodationService accommodationService;

    public Feedback addFeedback(
            Long accommodationId,
            Long reservationId,
            Feedback feedback,
            String email) {

        // Check if feedback already submitted for this reservation
        if (feedbackRepository.existsByReservationId(reservationId))
            return null;

        User user = userRepository.findByEmail(email);
        Accommodation accommodation = accommodationService
                .retrieveAccommodation(accommodationId);
        Reservation reservation = reservationRepository
                .findById(reservationId).orElse(null);

        if (user == null || accommodation == null || reservation == null)
            return null;

        feedback.setUser(user);
        feedback.setAccommodation(accommodation);
        feedback.setReservation(reservation);
        return feedbackRepository.save(feedback);
    }
    public List<Feedback> getFeedbackByAccommodation(Long accommodationId) {
        return feedbackRepository.findByAccommodationId(accommodationId);
    }
    public boolean hasFeedback(Long reservationId) {
        return feedbackRepository.existsByReservationId(reservationId);
    }
}