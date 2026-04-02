package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;

import java.util.List;

public interface IReservationService {

    List<Reservation> retrieveAllReservations();

    Reservation retrieveReservation(Long id);

    Reservation addReservation(Reservation reservation);

    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation);

    List<Reservation> retrieveRestaurantReservations(Long restaurantId, ReservationStatus status);

    Reservation confirmRestaurantReservation(Long reservationId, List<Long> tableIds);

    Reservation cancelReservation(Long reservationId);

    Reservation completeReservation(Long reservationId);
}
