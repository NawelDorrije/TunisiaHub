package org.example.backend_tunisiahub.carpooling.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.carpooling.dto.TripCreateRequest;
import org.example.backend_tunisiahub.carpooling.dto.TripResponse;
import org.example.backend_tunisiahub.carpooling.dto.TripUpdateRequest;
import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.example.backend_tunisiahub.carpooling.entity.TripStatus;
import org.example.backend_tunisiahub.carpooling.repository.TripRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripService {

    private final TripRepository tripRepository;

    @Transactional
    public TripResponse createTrip(TripCreateRequest request, Long driverId) {
        log.debug("Creating trip in service for driverId={} with seatsTotal={} and price={}",
            driverId,
            request.getSeatsTotal(),
            request.getPrice());
        validateFutureDate(request.getDepartureDateTime());

        Trip trip = new Trip();
        trip.setDriverId(driverId);
        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setStatus(TripStatus.PLANNED);

        Trip saved = tripRepository.save(trip);
        log.debug("Trip persisted with id={} for driverId={}", saved.getId(), driverId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> searchTrips(String departurePoint, String destination, LocalDate date) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return tripRepository.findAll(specification).stream()
                .sorted(Comparator.comparing(Trip::getDepartureDateTime))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));
        return toResponse(trip);
    }

    @Transactional(readOnly = true)
    public List<TripResponse> getMyTrips(Long driverId) {
        return tripRepository.findByDriverIdOrderByDepartureDateTimeDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TripResponse cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getDriverId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own trip");
        }

        if (trip.getStatus() != TripStatus.CANCELLED) {
            trip.setStatus(TripStatus.CANCELLED);
            trip = tripRepository.save(trip);
        }

        return toResponse(trip);
    }

    @Transactional
    public TripResponse updateTrip(Long tripId, Long currentUserId, TripUpdateRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trip not found"));

        if (!trip.getDriverId().equals(currentUserId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only edit your own trip");
        }

        if (trip.getStatus() == TripStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled trips cannot be edited");
        }

        validateFutureDate(request.getDepartureDateTime());

        trip.setDeparturePoint(request.getDeparturePoint().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        trip.setSeatsAvailable(request.getSeatsTotal());

        Trip saved = tripRepository.save(trip);
        return toResponse(saved);
    }

    private void validateFutureDate(LocalDateTime departureDateTime) {
        if (departureDateTime == null || !departureDateTime.isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "departureDateTime must be in the future");
        }
    }

    private TripResponse toResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getDriverId(),
                trip.getDeparturePoint(),
                trip.getDestination(),
                trip.getDepartureDateTime(),
                trip.getPrice(),
                trip.getSeatsTotal(),
                trip.getSeatsAvailable(),
                trip.getStatus(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
