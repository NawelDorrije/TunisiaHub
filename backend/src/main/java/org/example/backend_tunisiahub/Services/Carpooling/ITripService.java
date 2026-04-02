package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;

import java.time.LocalDate;
import java.util.List;

public interface ITripService {

    List<Trip> retrieveAllTrips(String departurePoint, String destination, LocalDate date, Integer seatsRequired);

    Trip retrieveTrip(Long id);

    List<Trip> retrieveMyTrips(Long driverId);

    Trip addTrip(Trip trip, Long driverId);

    Trip modifyTrip(Long tripId, Trip trip, Long currentUserId);

    Trip cancelTrip(Long tripId, Long currentUserId);

    Trip makeTripAvailable(Long tripId, Long currentUserId);
}
