package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.Carpooling.VehicleRepository;
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
    private VehicleRepository vehicleRepository;
    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_CANCELED = "canceled";

    @Override
    public List<Trip> retrieveAllTrips(String departurePoint, String destination, LocalDate date, Integer seatsRequired) {
        return tripRepository.findByStatusIgnoreCaseOrderByDepartureDateTimeAsc(STATUS_SCHEDULED)
                .stream()
                .filter(trip -> departurePoint == null
                        || departurePoint.isBlank()
                        || trip.getDeparturePoint().toLowerCase().contains(departurePoint.trim().toLowerCase()))
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
        return tripRepository.findByCreatedByOrderByDepartureDateTimeDesc(String.valueOf(driverId));
    }

    @Override
    public Trip addTrip(Trip request, Long driverId) {
        validateTrip(request);

        Trip trip = new Trip();
        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setStatus(STATUS_SCHEDULED);
        trip.setCreatedBy(String.valueOf(driverId));
        trip.setVehicle(resolveVehicle(request.getVehicle(), driverId));

        return tripRepository.save(trip);
    }

    @Override
    public Trip modifyTrip(Long tripId, Trip request, Long currentUserId) {
        validateTrip(request);

        Trip trip = tripRepository.findById(tripId).get();
        if (!String.valueOf(currentUserId).equals(trip.getCreatedBy())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own trip");
        }

        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        if (request.getVehicle() != null) {
            trip.setVehicle(resolveVehicle(request.getVehicle(), currentUserId));
        }

        return tripRepository.save(trip);
    }

    @Override
    public Trip cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).get();
        if (!String.valueOf(currentUserId).equals(trip.getCreatedBy())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own trip");
        }
        trip.setStatus(STATUS_CANCELED);
        return tripRepository.save(trip);
    }

    private Vehicle resolveVehicle(Vehicle vehicle, Long driverId) {
        if (vehicle != null && vehicle.getId() != null) {
            Vehicle existingVehicle = vehicleRepository.findByIdAndOwnerId(vehicle.getId(), String.valueOf(driverId));
            if (existingVehicle == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Selected vehicle must belong to the driver");
            }
            return existingVehicle;
        }

        List<Vehicle> vehicles = vehicleRepository.findByOwnerIdOrderByIdDesc(String.valueOf(driverId));
        if (!vehicles.isEmpty()) {
            return vehicles.get(0);
        }
        return null;
    }

    private void validateTrip(Trip request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trip is required");
        }
        if (request.getDeparturePoint() == null || request.getDeparturePoint().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departurePoint is required");
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
}
