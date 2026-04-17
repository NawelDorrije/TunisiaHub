package org.example.backend_tunisiahub.Services.Carpooling;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.Carpooling.VehicleRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImp implements org.example.backend_tunisiahub.Services.Carpooling.ITripService {

    private final TripRepository tripRepository;
    private final VehicleRepository vehicleRepository;
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_CANCELED = "CANCELED";

    @Transactional
    @Override
    public Trip createTrip(Trip request, Long driverId) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trip payload is required");
        }
        log.debug("Creating trip in service for driverId={} with seatsTotal={} and price={}",
            driverId,
            request.getSeatsTotal(),
            request.getPrice());
        validateTripPayload(request);
        validateFutureDate(request.getDepartureDateTime());
        Vehicle vehicle = resolveDriverVehicle(resolveVehicleId(request), driverId);

        Trip trip = new Trip();
        trip.setCreatedBy(String.valueOf(driverId));
        trip.setDriverId(String.valueOf(driverId));
        trip.setVehicle(vehicle);
        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setStatus(STATUS_SCHEDULED);

        Trip saved = tripRepository.save(trip);
        log.debug("Trip persisted with id={} for driverId={}", saved.getId(), driverId);
        return saved;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Trip> searchTrips(String departurePoint, String destination, LocalDate date) {
        Specification<Trip> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (departurePoint != null && !departurePoint.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("departurePoint")),
                        "%" + departurePoint.trim().toLowerCase() + "%"
                ));
            }

            if (destination != null && !destination.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("destination")),
                        "%" + destination.trim().toLowerCase() + "%"
                ));
            }

            if (date != null) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureDateTime"), start));
                predicates.add(cb.lessThan(root.get("departureDateTime"), end));
            }

            predicates.add(cb.equal(root.get("status"), STATUS_SCHEDULED));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return tripRepository.findAll(specification).stream()
            .sorted(Comparator.comparing(Trip::getDepartureDateTime))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Trip> searchPublicTrips(String departurePoint,
                                        String destination,
                                        LocalDate date,
                                        Integer seatsRequired,
                                        Pageable pageable) {
        Specification<Trip> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (departurePoint != null && !departurePoint.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("departurePoint")),
                        "%" + departurePoint.trim().toLowerCase() + "%"
                ));
            }

            if (destination != null && !destination.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("destination")),
                        "%" + destination.trim().toLowerCase() + "%"
                ));
            }

            if (date != null) {
                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureDateTime"), start));
                predicates.add(cb.lessThan(root.get("departureDateTime"), end));
            }

            if (seatsRequired != null && seatsRequired > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("seatsAvailable"), seatsRequired));
            }

            predicates.add(cb.equal(root.get("status"), STATUS_SCHEDULED));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return tripRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public Trip getPublicTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!STATUS_SCHEDULED.equals(trip.getStatus())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Trip not found");
        }

        return trip;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Trip> getMyTrips(Long driverId, Pageable pageable) {
        return tripRepository.findByCreatedByOrderByDepartureDateTimeDesc(String.valueOf(driverId), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Trip getMyTripById(Long tripId, Long driverId) {
        return tripRepository.findByIdAndCreatedBy(tripId, String.valueOf(driverId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Trip> getMyTripsForLegacyClient(Long driverId) {
        return tripRepository.findByCreatedByOrderByDepartureDateTimeDesc(String.valueOf(driverId));
    }

    @Transactional
    @Override
    public Trip cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!String.valueOf(currentUserId).equals(trip.getCreatedBy())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own trip");
        }

        if (!STATUS_CANCELED.equals(trip.getStatus())) {
            trip.setStatus(STATUS_CANCELED);
            trip = tripRepository.save(trip);
        }

        return trip;
    }

    @Transactional
    @Override
    public Trip updateTrip(Long tripId, Long currentUserId, Trip request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Trip payload is required");
        }
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!String.valueOf(currentUserId).equals(trip.getCreatedBy())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own trip");
        }

        if (STATUS_CANCELED.equals(trip.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled trips cannot be edited");
        }

        validateTripPayload(request);
        validateFutureDate(request.getDepartureDateTime());
        Long requestVehicleId = resolveVehicleId(request);
        Vehicle vehicle = requestVehicleId == null
            ? trip.getVehicle()
            : resolveDriverVehicle(requestVehicleId, currentUserId);

        if (request.getSeatsTotal() < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "seatsTotal must be greater than 0");
        }

        if (trip.getSeatsAvailable() != trip.getSeatsTotal()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "seatsTotal cannot be changed after bookings start");
        }

        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setVehicle(vehicle);

        return tripRepository.save(trip);
    }

    private void validateFutureDate(LocalDateTime departureDateTime) {
        if (departureDateTime == null || !departureDateTime.isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departureTime must be in the future");
        }
    }

    private Vehicle resolveDriverVehicle(Long vehicleId, Long driverId) {
        if (vehicleId == null) {
            Page<Vehicle> existingVehicles = vehicleRepository.findByOwnerIdOrderByIdDesc(
                    String.valueOf(driverId),
                    PageRequest.of(0, 1)
            );
            if (!existingVehicles.isEmpty()) {
                return existingVehicles.getContent().get(0);
            }

            Vehicle defaultVehicle = new Vehicle();
            defaultVehicle.setOwnerId(String.valueOf(driverId));
            defaultVehicle.setModel("Default vehicle");
            defaultVehicle.setColor("Gray");
            defaultVehicle.setPlateNumber("AUTO-" + driverId + "-" + System.currentTimeMillis());
            return vehicleRepository.save(defaultVehicle);
        }
        return vehicleRepository.findByIdAndOwnerId(vehicleId, String.valueOf(driverId))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected vehicle must belong to the driver"));
    }

    private Long resolveVehicleId(Trip request) {
        if (request.getVehicle() == null || request.getVehicle().getId() == null) {
            return null;
        }
        return request.getVehicle().getId();
    }

    private void validateTripPayload(Trip request) {
        if (request.getDeparturePoint() == null || request.getDeparturePoint().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departurePoint is required");
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "destination is required");
        }
        if (request.getDepartureDateTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departureDateTime is required");
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
