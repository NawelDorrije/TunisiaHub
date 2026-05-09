package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface IReservationRestaurantService {

    List<ReservationRestaurant> retrieveAllReservations();

    ReservationRestaurant retrieveReservation(Long id);

    ReservationRestaurant addReservation(ReservationRestaurant reservation);

    void deleteReservation(Long id);

    ReservationRestaurant modifyReservation(ReservationRestaurant reservation);

    List<ReservationRestaurant> retrieveRestaurantReservations(Long restaurantId, ReservationRestaurantStatus status);

    List<ReservationRestaurant> retrieveReservationsByUser(Long userId);

    List<ReservationRestaurant> retrieveMyReservations();

    ReservationRestaurant confirmRestaurantReservation(Long reservationId, List<Long> tableIds);

    boolean isTableAvailableForReservation(Long restaurantId, Long tableId, LocalDateTime dateTime, Long excludedReservationId);

    ReservationRestaurant cancelReservation(Long reservationId);

    ReservationRestaurant completeReservation(Long reservationId);

    ReservationRestaurant checkInReservation(String token);
}
