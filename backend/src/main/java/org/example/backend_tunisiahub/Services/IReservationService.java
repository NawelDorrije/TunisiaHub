package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

public interface IReservationService {

    List<Reservation> retrieveAllReservations();

    List<Reservation> retrieveReservationsByUserId(Long userId);

    List<Reservation> retrieveReservationsByTripId(Long tripId, Long currentUserId);

    ReservationQuote calculateTripQuote(Long tripId, Integer seatsRequested);

    Reservation retrieveReservation(Long id);

    Reservation addReservation(Reservation reservation, Long currentUserId);

    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation, Long currentUserId);

    Reservation approveReservation(Long reservationId, Long currentUserId);

    Reservation rejectReservation(Long reservationId, Long currentUserId);
}
