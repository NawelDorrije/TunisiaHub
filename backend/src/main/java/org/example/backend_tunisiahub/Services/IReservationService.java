package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationService {

    // Restaurant reservation methods
    List<Reservation> retrieveAllReservations();

    Reservation retrieveReservation(Long id);

    Reservation addReservation(Reservation reservation);

    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation);

    List<Reservation> retrieveRestaurantReservations(Long restaurantId, ReservationStatus status);

    List<Reservation> retrieveReservationsByUser(Long userId);

    List<Reservation> retrieveMyReservations();

    Reservation confirmRestaurantReservation(Long reservationId, List<Long> tableIds);

    boolean isTableAvailableForReservation(Long restaurantId, Long tableId, LocalDateTime dateTime, Long excludedReservationId);

    Reservation cancelReservation(Long reservationId);

    Reservation completeReservation(Long reservationId);

    Reservation checkInReservation(String token);
}
