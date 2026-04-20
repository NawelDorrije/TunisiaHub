package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELED = "CANCELED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String BOOKING_MODE_INSTANT = "instant";

    private final ReservationRepository reservationRepository;
    private final TripRepository tripRepository;
    private final ReservationPricingService reservationPricingService;
    private final UserRepository userRepository;

    @Override
    public List<Reservation> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public List<Reservation> retrieveReservationsByUserId(Long userId) {
        return reservationRepository.findByReservedBy_Id(userId);
    }

    @Override
    public List<Reservation> retrieveReservationsByTripId(Long tripId, Long currentUserId) {
        if (tripId == null || currentUserId == null) {
            return List.of();
        }

        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null || trip.getDriver() == null || !currentUserId.equals(trip.getDriver().getId())) {
            return List.of();
        }

        return reservationRepository.findByTripIdOrderByIdDesc(tripId);
    }

    @Override
    public ReservationQuote calculateTripQuote(Long tripId, Integer seatsRequested) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            return new ReservationQuote(0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return reservationPricingService.calculateQuote(
                trip.getPrice(),
                seatsRequested == null ? 1 : seatsRequested
        );
    }

    @Override
    public Reservation retrieveReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation addReservation(Reservation reservation, Long currentUserId) {
        if (!prepareReservation(reservation, currentUserId)) {
            return null;
        }
        applyTripReservationStatusOnCreate(reservation);
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }

    @Override
    public Reservation modifyReservation(Reservation reservation, Long currentUserId) {
        if (!prepareReservation(reservation, currentUserId)) {
            return null;
        }
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation approveReservation(Long reservationId, Long currentUserId) {
        Reservation reservation = reservationRepository.findByIdAndTrip_Driver_Id(reservationId, currentUserId);
        if (!isPendingTripReservation(reservation)) {
            return null;
        }

        reservation.setStatus(STATUS_CONFIRMED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation rejectReservation(Long reservationId, Long currentUserId) {
        Reservation reservation = reservationRepository.findByIdAndTrip_Driver_Id(reservationId, currentUserId);
        if (!isPendingTripReservation(reservation)) {
            return null;
        }

        reservation.setStatus(STATUS_CANCELED);
        return reservationRepository.save(reservation);
    }

    private boolean prepareReservation(Reservation reservation, Long currentUserId) {
        if (reservation == null) {
            return false;
        }

        if (currentUserId == null) {
            return false;
        }

        User user = userRepository.findById(currentUserId).orElse(null);
        if (user == null) {
            return false;
        }
        reservation.setReservedBy(user);

        if (reservation.getNumberOfPeople() == null) {
            if (reservation.getType() == ReservationType.TripReservation) {
                reservation.setNumberOfPeople(1);
            } else {
                reservation.setNumberOfPeople(0);
            }
        }

        if (reservation.getType() == ReservationType.TripReservation
                && reservation.getTrip() != null
                && reservation.getTrip().getId() != null) {
            Trip trip = tripRepository.findById(reservation.getTrip().getId()).orElse(null);
            if (trip == null) {
                return false;
            }

            if (isActiveTripReservation(reservation)
                    && !hasEnoughTripSeats(trip, reservation.getId(), reservation.getNumberOfPeople())) {
                return false;
            }

            reservation.setTrip(trip);
            ReservationQuote quote = reservationPricingService.calculateQuote(
                    trip.getPrice(),
                    reservation.getNumberOfPeople()
            );
            reservation.setTotalPrice(quote.totalAmount().doubleValue());
        }

        return true;
    }

    private void applyTripReservationStatusOnCreate(Reservation reservation) {
        if (reservation == null || reservation.getType() != ReservationType.TripReservation) {
            return;
        }

        Trip trip = reservation.getTrip();
        if (trip == null) {
            return;
        }

        String bookingMode = trip.getBookingMode() == null ? "" : trip.getBookingMode().trim();
        if (BOOKING_MODE_INSTANT.equalsIgnoreCase(bookingMode)) {
            reservation.setStatus(STATUS_CONFIRMED);
            return;
        }

        reservation.setStatus(STATUS_PENDING);
    }

    private boolean hasEnoughTripSeats(Trip trip, Long reservationId, Integer requestedSeats) {
        int seatsRequested = requestedSeats == null || requestedSeats < 1 ? 1 : requestedSeats;
        int reservedSeats = reservationRepository.findByTripId(trip.getId()).stream()
                .filter(this::isActiveTripReservation)
                .filter(reservation -> reservationId == null || !reservationId.equals(reservation.getId()))
                .mapToInt(this::getReservedPeopleCount)
                .sum();
        return trip.getSeatsTotal() - reservedSeats >= seatsRequested;
    }

    private boolean isActiveTripReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        String status = reservation.getStatus() == null ? "" : reservation.getStatus();
        return !status.equalsIgnoreCase(STATUS_CANCELED) && !status.equalsIgnoreCase(STATUS_CANCELLED);
    }

    private boolean isPendingTripReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        if (reservation.getType() != ReservationType.TripReservation || reservation.getTrip() == null) {
            return false;
        }

        String status = reservation.getStatus() == null ? "" : reservation.getStatus();
        return status.equalsIgnoreCase(STATUS_PENDING);
    }

    private int getReservedPeopleCount(Reservation reservation) {
        Integer numberOfPeople = reservation.getNumberOfPeople();
        if (numberOfPeople == null || numberOfPeople < 1) {
            return 1;
        }
        return numberOfPeople;
    }
}
