package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TripServiceImp implements ITripService {

    private TripRepository tripRepository;
    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_CANCELED = "canceled";
    private static final String BOOKING_MODE_INSTANT = "instant";
    private static final String BOOKING_MODE_MANUAL = "manual";

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
        return tripRepository.findById(id).get();
    }

    @Override
    public List<Trip> retrieveMyTrips(Long driverId) {
        String currentUserId = String.valueOf(driverId);
        return tripRepository.findByCreatedByOrderByDepartureDateTimeDesc(currentUserId);
    }

    @Override
    public Trip addTrip(Trip request, Long driverId) {
        validateTrip(request);

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
        trip.setCreatedBy(String.valueOf(driverId));
        return tripRepository.save(trip);
    }

    @Override
    public Trip modifyTrip(Long tripId, Trip request, Long currentUserId) {
        validateTrip(request);

        Trip trip = tripRepository.findById(tripId).get();
        if (!isOwner(trip, currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own trip");
        }

        trip.setDeparture(request.getDeparture().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            trip.setDurationMinutes(request.getDurationMinutes());
        }
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        if (request.getBookingMode() != null && !request.getBookingMode().isBlank()) {
            trip.setBookingMode(normalizeBookingMode(request.getBookingMode()));
        }
        return tripRepository.save(trip);
    }

    @Override
    public Trip cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).get();
        if (!isOwner(trip, currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own trip");
        }
        trip.setStatus(STATUS_CANCELED);
        return tripRepository.save(trip);
    }

    @Override
    public Trip makeTripAvailable(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).get();
        if (!isOwner(trip, currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only update your own trip");
        }
        trip.setStatus(STATUS_SCHEDULED);
        return tripRepository.save(trip);
    }

    private boolean isOwner(Trip trip, Long currentUserId) {
        String userId = String.valueOf(currentUserId);
        return userId.equals(trip.getCreatedBy());
    }

    private void validateTrip(Trip request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trip is required");
        }
        if (request.getDeparture() == null || request.getDeparture().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departure is required");
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "destination is required");
        }
        if (request.getDepartureDateTime() == null || !request.getDepartureDateTime().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departureDateTime must be in the future");
        }
        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "price must be greater than or equal to 0");
        }
        if (request.getSeatsTotal() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "seatsTotal must be greater than 0");
        }
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
