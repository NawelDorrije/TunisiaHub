package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Services.Carpooling.ITripService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/driver/trips")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver Trips")
public class TripController {

    private final ITripService tripService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @Operation(summary = "Publish a trip")
    public ResponseEntity<Trip> createTrip(@Valid @RequestBody TripWriteRequest request,
                                           HttpServletRequest httpRequest) {
        Long currentUserId = currentUserResolver.getUserId(httpRequest);
        ensureUserRole(httpRequest);
        log.info("Create trip request received for driverId={}, departure={}, destination={}, departureTime={}",
            currentUserId,
            request.departurePoint(),
            request.destination(),
            request.departureDateTime());
        Trip response = tripService.createTrip(toTrip(request), currentUserId);
        log.info("Create trip succeeded for driverId={}, tripId={}", currentUserId, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List current driver trips")
    public Page<Trip> getMyTrips(HttpServletRequest request,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureUserRole(request);
        return tripService.getMyTrips(currentUserId, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current driver trip details")
    public Trip getTripById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureUserRole(request);
        return tripService.getMyTripById(id, currentUserId);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel trip")
    public Trip cancelTrip(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureUserRole(request);
        return tripService.cancelTrip(id, currentUserId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trip")
    public Trip updateTrip(@PathVariable Long id,
                           @Valid @RequestBody TripWriteRequest payload,
                           HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureUserRole(request);
        return tripService.updateTrip(id, currentUserId, toTrip(payload));
    }

    private Trip toTrip(TripWriteRequest payload) {
        Trip trip = new Trip();
        trip.setDeparturePoint(payload.departurePoint());
        trip.setDestination(payload.destination());
        trip.setDepartureDateTime(payload.departureDateTime());
        trip.setPrice(payload.price());
        trip.setSeatsTotal(payload.seatsTotal());
        if (payload.vehicleId() != null) {
            Vehicle vehicle = new Vehicle();
            vehicle.setId(payload.vehicleId());
            trip.setVehicle(vehicle);
        }
        return trip;
    }


    private void ensureUserRole(HttpServletRequest request) {
        String role = currentUserResolver.getRole(request);
        if (!"USER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with USER role can perform this action");
        }
    }

    private record TripWriteRequest(
            @NotBlank String departurePoint,
            @NotBlank String destination,
            @NotNull LocalDateTime departureDateTime,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
            @Min(1) int seatsTotal,
            Long vehicleId
    ) {
    }

    private record TripView(
            Long id,
            String departurePoint,
            String destination,
            LocalDateTime departureDateTime,
            BigDecimal price,
            int seatsTotal,
            int seatsAvailable,
            String status,
            LocalDateTime createdAt,
            String createdBy,
            Long vehicleId
    ) {
    }
}
