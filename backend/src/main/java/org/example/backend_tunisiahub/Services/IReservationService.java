package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

public interface IReservationService {

    List<Reservation> retrieveAllReservations();

    List<Reservation> retrieveReservationsByUserId(Long userId);

    Reservation retrieveReservation(Long id);

    Reservation addReservation(Reservation reservation);

    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation);
}
