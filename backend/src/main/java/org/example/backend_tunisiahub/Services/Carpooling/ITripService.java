package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ITripService {

    Trip createTrip(Trip request, Long driverId);

    List<Trip> searchTrips(String departurePoint, String destination, LocalDate date);

    Page<Trip> searchPublicTrips(String departurePoint,
                                 String destination,
                                 LocalDate date,
                                 Integer seatsRequired,
                                 Pageable pageable);

    Trip getTripById(Long id);

    Trip getPublicTripById(Long id);

    Page<Trip> getMyTrips(Long driverId, Pageable pageable);

    Trip getMyTripById(Long tripId, Long driverId);

    List<Trip> getMyTripsForLegacyClient(Long driverId);

    Trip cancelTrip(Long tripId, Long currentUserId);

    Trip updateTrip(Long tripId, Long currentUserId, Trip request);
}
