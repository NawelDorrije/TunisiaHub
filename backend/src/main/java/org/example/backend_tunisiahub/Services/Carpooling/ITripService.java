package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ITripService {

    List<Trip> retrieveAllTrips(String departurePoint,
                                String destination,
                                LocalDate dateFrom,
                                LocalDate dateTo,
                                Integer seatsRequired,
                                String status,
                                String bookingMode,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                Integer durationMax);

    Trip retrieveTrip(Long id);

    Trip retrieveMyTrip(Long id, Long driverId);

    List<Trip> retrieveMyTrips(Long driverId);

    Integer retrieveTripSeatsAvailable(Long tripId);

    Trip addTrip(Trip trip, Long driverId);

    Trip modifyTrip(Long tripId, Trip trip, Long currentUserId);

    Trip cancelTrip(Long tripId, Long currentUserId);

    Trip makeTripAvailable(Long tripId, Long currentUserId);
}
