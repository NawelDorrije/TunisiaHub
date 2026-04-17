package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

public interface IReservationService {

    List<Reservation> retrieveAllReservations();

    Reservation retrieveReservation(Long id);

    Reservation addReservation(Reservation reservation);

    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation);
    Reservation reserveEvent(Long userId, Long eventId);
    Reservation createPendingReservation(Long userId, Long eventId);

    Reservation confirmReservation(Long reservationId);
    Reservation getUserReservationForEvent(Long userId, Long eventId);
    Reservation findByUserAndEvent(Long userId, Long eventId);
}
