package org.example.backend_tunisiahub.Services;


import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;

import java.util.List;

public interface IReservationService {
    ReservationDTO createReservation(ReservationDTO dto);
    ReservationDTO getById(Long id);
    List<ReservationDTO> getAll();
    List<ReservationDTO> getByUser(Long userId);
    List<ReservationDTO> getBySpot(Long spotId);
    List<ReservationDTO> getByStatus(ReservationStatus status);
    ReservationDTO updateStatus(Long id, ReservationStatus status);
    void cancelReservation(Long id);
}