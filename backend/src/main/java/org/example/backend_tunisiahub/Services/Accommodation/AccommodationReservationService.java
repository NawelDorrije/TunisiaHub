package org.example.backend_tunisiahub.Services.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.Accommodation.AccommodationStatsDTO;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationReservationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.ReviewRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Services.Accommodation.Notification.ReservationEmailService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
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
    final ReviewRepository reviewRepository;
    final AccommodationRepository accommodationRepository;
        final ReservationEmailService reservationEmailService;

    @Override
    public Reservation addAccommodationReservation(Long accommodationId, Reservation reservation, String email) {
        if (reservation == null || reservation.getStartDate() == null || reservation.getEndDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start and end dates are required");
        }
        if (!reservation.getEndDate().after(reservation.getStartDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        // Check availability
        if (!isAccommodationAvailable(accommodationId, reservation.getStartDate(), reservation.getEndDate())) {
            throw new ApiException(HttpStatus.CONFLICT, "Accommodation is not available for the selected dates");
        }

        // Get accommodation
        Accommodation accommodation = accommodationService.retrieveAccommodation(accommodationId);
        if (accommodation == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Accommodation not found");
        }

        // Get user
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }

        // Calculate total price
        long diffInMillis = reservation.getEndDate().getTime() - reservation.getStartDate().getTime();
        long nights = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        BigDecimal totalPrice = BigDecimal.valueOf(accommodation.getPrice())
                .multiply(BigDecimal.valueOf(nights));

        // Set reservation fields
        reservation.setAccommodation(accommodation);
        reservation.setUser(user);
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setType(ReservationType.accommodationReservation);
        reservation.setReminderStatus("PENDING");
        reservation.setReminderSentAt(null);
        reservation.setReminderError(null);

        Reservation savedReservation = reservationRepository.save(reservation);
        try {
            reservationEmailService.sendAccommodationBookingConfirmation(user, accommodation, savedReservation);
        } catch (Exception ex) {
            // Reservation is already persisted; notification must not break booking.
        }

        return savedReservation;
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
        reservation.setStatus(ReservationStatus.CANCELLED);
                reservation.setReminderStatus("CANCELLED");
                reservation.setReminderError(null);
        return reservationRepository.save(reservation);
    }

    @Override
    public List<Map<String, Date>> getReservedDates(Long accommodationId) {
        return reservationRepository.findByAccommodationId(accommodationId)
                .stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
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
        BigDecimal totalPrice = BigDecimal.valueOf(existing.getAccommodation().getPrice())
                .multiply(BigDecimal.valueOf(nights));

        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setTotalPrice(totalPrice);
                existing.setReminderSentAt(null);
                existing.setReminderStatus("PENDING");
                existing.setReminderError(null);

        return reservationRepository.save(existing);
    }
    @Override
    public AccommodationStatsDTO getStatistics() {
        AccommodationStatsDTO stats = new AccommodationStatsDTO();

        // All data
        List<Accommodation> allAccommodations = accommodationRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getType() != null &&
                        r.getType().name().equals("accommodationReservation"))
                .collect(Collectors.toList());

        // KPI cards
        stats.setTotalAccommodations(allAccommodations.size());
        stats.setTotalReservations(allReservations.size());

        double totalRevenue = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice().doubleValue() : 0.0)
                .sum();
        stats.setTotalRevenue(totalRevenue);

        // Average rating from reviews
        List<AccommodationReview> allReviews = reviewRepository.findAll();
        stats.setTotalReviews(allReviews.size());
        double avgRating = allReviews.stream()
                .mapToInt(AccommodationReview::getRating)
                .average()
                .orElse(0.0);
        stats.setAverageRating(Math.round(avgRating * 10.0) / 10.0);

        // Type distribution
        Map<String, Long> byType = allAccommodations.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getType() != null ? a.getType() : "Unknown",
                        Collectors.counting()
                ));
        stats.setAccommodationsByType(byType);

        // Reservation status
        stats.setConfirmedReservations(allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count());
        stats.setCancelledReservations(allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CANCELLED).count());

        // Price distribution
        stats.setUnder100(allAccommodations.stream()
                .filter(a -> a.getPrice() < 100).count());
        stats.setBetween100and200(allAccommodations.stream()
                .filter(a -> a.getPrice() >= 100 && a.getPrice() < 200).count());
        stats.setBetween200and300(allAccommodations.stream()
                .filter(a -> a.getPrice() >= 200 && a.getPrice() < 300).count());
        stats.setAbove300(allAccommodations.stream()
                .filter(a -> a.getPrice() >= 300).count());

        // Capacity distribution
        stats.setCapacity1to2(allAccommodations.stream()
                .filter(a -> a.getCapacite() <= 2).count());
        stats.setCapacity3to5(allAccommodations.stream()
                .filter(a -> a.getCapacite() >= 3 && a.getCapacite() <= 5).count());
        stats.setCapacity6to10(allAccommodations.stream()
                .filter(a -> a.getCapacite() >= 6 && a.getCapacite() <= 10).count());
        stats.setCapacityAbove10(allAccommodations.stream()
                .filter(a -> a.getCapacite() > 10).count());

        // Top 5 profitable accommodations
        Map<Long, Double> revenueByAccommodation = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                        && r.getAccommodation() != null
                        && r.getTotalPrice() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getAccommodation().getId(),
                        Collectors.summingDouble(r -> r.getTotalPrice().doubleValue())
                ));

        List<Map<String, Object>> topProfitable = revenueByAccommodation.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Accommodation acc = accommodationRepository
                            .findById(entry.getKey()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", entry.getKey());
                    map.put("title", acc != null ? acc.getTitle() : "Unknown");
                    map.put("type", acc != null ? acc.getType() : "Unknown");
                    map.put("revenue", entry.getValue());
                    map.put("price", acc != null ? acc.getPrice() : 0);
                    return map;
                })
                .collect(Collectors.toList());
        stats.setTopProfitableAccommodations(topProfitable);

        // Top 5 most reserved accommodations
        Map<Long, Long> reservationsByAccommodation = allReservations.stream()
                .filter(r -> r.getAccommodation() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getAccommodation().getId(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> topReserved = reservationsByAccommodation.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Accommodation acc = accommodationRepository
                            .findById(entry.getKey()).orElse(null);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", entry.getKey());
                    map.put("title", acc != null ? acc.getTitle() : "Unknown");
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        stats.setTopReservedAccommodations(topReserved);

        return stats;
    }
}
