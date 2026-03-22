package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

public interface IReservationService {

    List<Reservation> retrieveAllReservations();

    Reservation retrieveReservation(Long id);

    Reservation addReservationCamping(ReservationDTO dto);
    void deleteReservation(Long id);

    Reservation modifyReservation(Reservation reservation);
}
