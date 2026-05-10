package org.example.backend_tunisiahub.Services.Accommodation;

import org.example.backend_tunisiahub.Entities.Accommodation.Feedback;

import java.util.List;

public interface IFeedbackService {
    public Feedback addFeedback(
            Long accommodationId,
            Long reservationId,
            Feedback feedback,
            String email);
    public List<Feedback> getFeedbackByAccommodation(Long accommodationId);
    public boolean hasFeedback(Long reservationId);

}
