package org.example.backend_tunisiahub.Entities.Camping.Mappers;


import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public ReservationDTO toDTO(Reservation r) {
        if (r == null) return null;

        Spot spot = r.getSpot();
        String campingName = null;
        if (spot != null && spot.getCamping() != null) {
            campingName = spot.getCamping().getName();
        }

        return ReservationDTO.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getNom() + " " + r.getUser().getNom())
                .spotId(spot != null ? spot.getId() : null)
                .spotName(spot != null ? spot.getName() : null)
                .campingName(campingName)
                .activityIds(r.getActivities().stream()
                        .map(a -> a.getId()).collect(Collectors.toList()))
                .activityNames(r.getActivities().stream()
                        .map(a -> a.getName()).collect(Collectors.toList()))
                .checkIn(r.getCheckIn())
                .checkOut(r.getCheckOut())
                .numberOfGuests(r.getNumberOfGuests())
                .totalPrice(r.getTotalPrice())
                .status(r.getStatus())
                .notes(r.getNotes())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
