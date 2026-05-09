<<<<<<< HEAD
package org.example.backend_tunisiahub.carpooling.service;

import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

=======
package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;

import java.math.BigDecimal;
>>>>>>> origin/feature/integrated-app-event
import java.time.LocalDate;
import java.util.List;

public interface ITripService {

<<<<<<< HEAD
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
=======
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

  TripPriceSuggestion retrievePriceSuggestion(String departure,
                                              String destination,
                                              LocalDate departureDate,
                                              Integer durationMinutes);

  Trip addTrip(Trip trip, Long driverId);

  Trip modifyTrip(Long tripId, Trip trip, Long currentUserId);

  Trip cancelTrip(Long tripId, Long currentUserId);

  Trip makeTripAvailable(Long tripId, Long currentUserId);

  Trip completeTrip(Long tripId, Long currentUserId);
>>>>>>> origin/feature/integrated-app-event
}
