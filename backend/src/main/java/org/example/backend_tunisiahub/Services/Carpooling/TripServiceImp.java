package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TripServiceImp implements ITripService {

    private TripRepository tripRepository;
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TripServiceImp.class);
    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_CANCELED = "canceled";
    private static final String BOOKING_MODE_INSTANT = "instant";
    private static final String BOOKING_MODE_MANUAL = "manual";
    private static final int MAX_SEATS_TOTAL = 8;
    private static final BigDecimal MAX_PRICE = new BigDecimal("500");

    @Override
    public List<Trip> retrieveAllTrips(String departurePoint, String destination, LocalDate date, Integer seatsRequired) {
        return tripRepository.findByStatusIgnoreCaseOrderByDepartureDateTimeAsc(STATUS_SCHEDULED)
                .stream()
                .filter(trip -> departurePoint == null
                        || departurePoint.isBlank()
                        || trip.getDeparture().toLowerCase().contains(departurePoint.trim().toLowerCase()))
                .filter(trip -> destination == null
                        || destination.isBlank()
                        || trip.getDestination().toLowerCase().contains(destination.trim().toLowerCase()))
                .filter(trip -> date == null || trip.getDepartureDateTime().toLocalDate().equals(date))
                .filter(trip -> seatsRequired == null || seatsRequired < 1 || trip.getSeatsAvailable() >= seatsRequired)
                .toList();
    }

    @Override
    public Trip retrieveTrip(Long id) {
        logger.debug("Retrieve public trip id={}", id);
        return tripRepository.findById(id).orElse(null);
    }

    @Override
    public Trip retrieveMyTrip(Long id, Long driverId) {
        logger.debug("Retrieve driver trip id={} driverId={}", id, driverId);
        return tripRepository.findByIdAndDriverId(id, driverId);
    }

    @Override
    public List<Trip> retrieveMyTrips(Long driverId) {
        logger.debug("Retrieve trips for driverId={}", driverId);
        return tripRepository.findByDriverIdOrderByDepartureDateTimeDesc(driverId);
    }

    @Override
    public Trip addTrip(Trip request, Long driverId) {
        if (!validateTrip(request, 0)) {
            logger.warn("Trip validation failed on create for driverId={}", driverId);
            return null;
        }

        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            logger.warn("Trip creation failed because driver not found driverId={}", driverId);
            return null;
        }

        Trip trip = new Trip();
        trip.setDeparture(request.getDeparture().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setDurationMinutes(request.getDurationMinutes());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setStatus(STATUS_SCHEDULED);
        trip.setBookingMode(normalizeBookingMode(request.getBookingMode()));
        trip.setDriver(driver);
        logger.debug("Saving new trip for driverId={} departure={} destination={}",
                driverId, trip.getDeparture(), trip.getDestination());
        return tripRepository.save(trip);
    }

    @Override
    public Trip modifyTrip(Long tripId, Trip request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip update failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip update rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        int reservedSeats = getReservedSeats(tripId);
        if (!validateTrip(request, reservedSeats)) {
            logger.warn("Trip validation failed on update id={} userId={} reservedSeats={}",
                    tripId, currentUserId, reservedSeats);
            return null;
        }

        trip.setDeparture(request.getDeparture().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setDurationMinutes(request.getDurationMinutes());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal() - reservedSeats);
        if (request.getBookingMode() != null && !request.getBookingMode().isBlank()) {
            trip.setBookingMode(normalizeBookingMode(request.getBookingMode()));
        }
        logger.debug("Saving updated trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip cancel failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip cancel rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        trip.setStatus(STATUS_CANCELED);
        logger.debug("Saving canceled trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip makeTripAvailable(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip restore failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip restore rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        trip.setStatus(STATUS_SCHEDULED);
        logger.debug("Saving restored trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    private boolean isOwner(Trip trip, Long currentUserId) {
        return trip.getDriver() != null && currentUserId.equals(trip.getDriver().getId());
    }

    private boolean validateTrip(Trip request, int reservedSeats) {
        if (request == null) {
            return false;
        }
        if (request.getDeparture() == null || request.getDeparture().isBlank()) {
            return false;
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            return false;
        }
        if (normalizeLocation(request.getDeparture()).equals(normalizeLocation(request.getDestination()))) {
            return false;
        }
        if (request.getDepartureDateTime() == null || !request.getDepartureDateTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() < 1) {
            return false;
        }
        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            return false;
        }
        if (request.getSeatsTotal() < 1) {
            return false;
        }
        if (request.getSeatsTotal() > MAX_SEATS_TOTAL) {
            return false;
        }
        if (request.getSeatsTotal() < reservedSeats) {
            return false;
        }
        return true;
    }

    private int getReservedSeats(Long tripId) {
        return reservationRepository.findByTripId(tripId).stream()
                .filter(this::isActiveTripReservation)
                .mapToInt(this::getReservedPeopleCount)
                .sum();
    }

    private boolean isActiveTripReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        String status = reservation.getStatus() == null ? "" : reservation.getStatus();
        return !status.equalsIgnoreCase("canceled") && !status.equalsIgnoreCase("cancelled");
    }

    private int getReservedPeopleCount(Reservation reservation) {
        Integer numberOfPeople = reservation.getNumberOfPeople();
        if (numberOfPeople == null || numberOfPeople < 1) {
            return 1;
        }
        return numberOfPeople;
    }

    private String normalizeLocation(String value) {
        return value.trim().toLowerCase();
    }

    private String normalizeBookingMode(String value) {
        if (value == null || value.isBlank()) {
            return BOOKING_MODE_MANUAL;
        }

        String normalized = value.trim().toLowerCase();
        if (BOOKING_MODE_INSTANT.equals(normalized)) {
            return BOOKING_MODE_INSTANT;
        }

        return BOOKING_MODE_MANUAL;
    }
}
