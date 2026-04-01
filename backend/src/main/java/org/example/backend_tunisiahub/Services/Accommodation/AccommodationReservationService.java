package org.example.backend_tunisiahub.Services.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationReservationRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Services.Accommodation.AccommodationService;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationReservationService implements IAccommodationReservationService {

    final AccommodationReservationRepository reservationRepository;
    final AccommodationService accommodationService;
    final UserRepository userRepository;

    @Override
    public Reservation addAccommodationReservation(Long accommodationId, Reservation reservation, String email) {
        // Check availability
        if (!isAccommodationAvailable(accommodationId, reservation.getStartDate(), reservation.getEndDate())) {
            return null; // dates already taken
        }

        // Get accommodation
        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        if (accommodation == null) return null;

        // Get user
        User user = userRepository.findByEmail(email);

        // Calculate total price
        long diffInMillis = reservation.getEndDate().getTime() - reservation.getStartDate().getTime();
        long nights = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        double totalPrice = nights * accommodation.getPrice();

        // Set reservation fields
        reservation.setAccommodation(accommodation);
        reservation.setUser(user);
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus("CONFIRMED");
        reservation.setType(ReservationType.accommodationReservation);

        return reservationRepository.save(reservation);
    }

    @Override
    public List<Reservation> getReservationsByAccommodation(Long accommodationId) {
        return reservationRepository.findByAccommodationId(accommodationId);
    }

    @Override
    public boolean isAccommodationAvailable(Long accommodationId, Date startDate, Date endDate) {
        return !reservationRepository.existsOverlappingReservation(accommodationId, startDate, endDate);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) return null;
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Map<String, Date>> getReservedDates(Long accommodationId) {
        return reservationRepository.findByAccommodationId(accommodationId)
                .stream()
                .filter(r -> r.getStatus().equals("CONFIRMED"))
                .map(r -> {
                    Map<String, Date> range = new HashMap<>();
                    range.put("startDate", r.getStartDate());
                    range.put("endDate", r.getEndDate());
                    return range;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<Reservation> getReservationsByUser(String email) {
        User user = userRepository.findByEmail(email);
        return reservationRepository.findByUserAndType(user, ReservationType.accommodationReservation);
    }
    @Override
    public Reservation editReservation(Long reservationId, Reservation updated, String email) {
        Reservation existing = reservationRepository.findById(reservationId).orElse(null);
        if (existing == null) return null;

        // Check availability excluding current reservation
        List<Reservation> overlapping = reservationRepository
                .findByAccommodationId(existing.getAccommodation().getId())
                .stream()
                .filter(r -> r.getStatus().equals("CONFIRMED"))
                .filter(r -> !r.getId().equals(reservationId)) // exclude current
                .filter(r -> r.getStartDate().before(updated.getEndDate())
                        && r.getEndDate().after(updated.getStartDate()))
                .collect(Collectors.toList());

        if (!overlapping.isEmpty()) return null;

        // Recalculate price
        long diffInMillis = updated.getEndDate().getTime() - updated.getStartDate().getTime();
        long nights = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        double totalPrice = nights * existing.getAccommodation().getPrice();

        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setTotalPrice(totalPrice);

        return reservationRepository.save(existing);
    }
}