package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Services.Carpooling.ITripService;
import org.example.backend_tunisiahub.Services.Carpooling.RouteSuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/trips")
@AllArgsConstructor
@Tag(name = "Driver Trips")
public class TripController {

    ITripService tripService;
    RouteSuggestionService routeSuggestionService;
    private static final Logger logger = LoggerFactory.getLogger(TripController.class);

    @PostMapping
    @Operation(summary = "Publish a trip")
    public ResponseEntity<Trip> addTrip(@RequestBody Trip request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);
        logger.debug("POST /api/driver/trips userId={} departure={} destination={}",
                currentUserId,
                request != null ? request.getDeparture() : null,
                request != null ? request.getDestination() : null);
        if (currentUserId == null) {
            logger.warn("Trip creation rejected because X-USER-ID header is missing or invalid");
            return ResponseEntity.badRequest().build();
        }

        Trip trip = tripService.addTrip(request, currentUserId);
        if (trip == null) {
            logger.warn("Trip creation failed for userId={}", currentUserId);
            return ResponseEntity.badRequest().build();
        }

        logger.info("Trip created id={} driverId={}", trip.getId(), currentUserId);
        return ResponseEntity.ok(trip);
    }

    @GetMapping
    @Operation(summary = "List current driver trips")
    public List<Trip> retrieveMyTrips(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        logger.debug("GET /api/driver/trips userId={}", currentUserId);
        if (currentUserId == null) {
            logger.warn("Trip list rejected because X-USER-ID header is missing or invalid");
            return new ArrayList<>();
        }
        return tripService.retrieveMyTrips(currentUserId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current driver trip details")
    public ResponseEntity<Trip> retrieveTrip(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        logger.debug("GET /api/driver/trips/{} userId={}", id, currentUserId);
        if (currentUserId == null) {
            logger.warn("Trip details rejected for id={} because X-USER-ID header is missing or invalid", id);
            return ResponseEntity.badRequest().build();
        }

        Trip trip = tripService.retrieveMyTrip(id, currentUserId);
        if (trip == null) {
            logger.warn("Trip details not found or not owned id={} userId={}", id, currentUserId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel trip")
    public ResponseEntity<Trip> cancelTrip(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        logger.debug("PUT /api/driver/trips/{}/cancel userId={}", id, currentUserId);
        if (currentUserId == null) {
            logger.warn("Trip cancel rejected for id={} because X-USER-ID header is missing or invalid", id);
            return ResponseEntity.badRequest().build();
        }

        Trip trip = tripService.cancelTrip(id, currentUserId);
        if (trip == null) {
            logger.warn("Trip cancel failed id={} userId={}", id, currentUserId);
            return ResponseEntity.notFound().build();
        }

        logger.info("Trip canceled id={} userId={}", id, currentUserId);
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{id}/available")
    @Operation(summary = "Make trip available again")
    public ResponseEntity<Trip> makeTripAvailable(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        logger.debug("PUT /api/driver/trips/{}/available userId={}", id, currentUserId);
        if (currentUserId == null) {
            logger.warn("Trip availability restore rejected for id={} because X-USER-ID header is missing or invalid", id);
            return ResponseEntity.badRequest().build();
        }

        Trip trip = tripService.makeTripAvailable(id, currentUserId);
        if (trip == null) {
            logger.warn("Trip availability restore failed id={} userId={}", id, currentUserId);
            return ResponseEntity.notFound().build();
        }

        logger.info("Trip restored id={} userId={}", id, currentUserId);
        return ResponseEntity.ok(trip);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trip")
    public ResponseEntity<Trip> modifyTrip(@PathVariable Long id,
                                           @RequestBody Trip payload,
                                           HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        logger.debug("PUT /api/driver/trips/{} userId={}", id, currentUserId);
        if (currentUserId == null) {
            logger.warn("Trip update rejected for id={} because X-USER-ID header is missing or invalid", id);
            return ResponseEntity.badRequest().build();
        }

        Trip trip = tripService.modifyTrip(id, payload, currentUserId);
        if (trip == null) {
            logger.warn("Trip update failed id={} userId={}", id, currentUserId);
            return ResponseEntity.badRequest().build();
        }

        logger.info("Trip updated id={} userId={}", id, currentUserId);
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/route-suggestions")
    @Operation(summary = "Get route suggestions between two points")
    public List<Map<String, Object>> getRouteSuggestions(
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng
    ) {
        return routeSuggestionService.getRouteSuggestions(startLat, startLng, endLat, endLng);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String value = request.getHeader("X-USER-ID");
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
