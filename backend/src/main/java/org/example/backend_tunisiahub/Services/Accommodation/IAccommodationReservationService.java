package org.example.backend_tunisiahub.Services.Accommodation;

import org.example.backend_tunisiahub.Entities.Reservation;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IAccommodationReservationService {

    Reservation addAccommodationReservation(Long accommodationId, Reservation reservation, String email);

    List<Reservation> getReservationsByAccommodation(Long accommodationId);

    boolean isAccommodationAvailable(Long accommodationId, Date startDate, Date endDate);

    Reservation cancelReservation(Long reservationId);
    List<Map<String, Date>> getReservedDates(Long accommodationId);
    List<Reservation> getReservationsByUser(String email);
    Reservation editReservation(Long reservationId, Reservation updated, String email);
}