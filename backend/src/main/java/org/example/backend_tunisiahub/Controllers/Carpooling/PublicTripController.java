package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Services.Carpooling.ITripService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/carpooling/trips")
@AllArgsConstructor
@Tag(name = "Public Trips")
public class PublicTripController {

    ITripService tripService;

    @GetMapping
    @Operation(summary = "Search public scheduled trips")
    public List<Trip> retrieveAllTrips(@RequestParam(required = false) String departurePoint,
                                       @RequestParam(required = false) String destination,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                       @RequestParam(required = false)
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
                                       @RequestParam(required = false) Integer seatsRequired,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String bookingMode,
                                       @RequestParam(required = false) BigDecimal minPrice,
                                       @RequestParam(required = false) BigDecimal maxPrice,
                                       @RequestParam(required = false) Integer durationMax) {
        return tripService.retrieveAllTrips(
                departurePoint,
                destination,
                dateFrom,
                dateTo,
                seatsRequired,
                status,
                bookingMode,
                minPrice,
                maxPrice,
                durationMax
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public trip details by id")
    public ResponseEntity<Trip> retrieveTrip(@PathVariable Long id) {
        Trip trip = tripService.retrieveTrip(id);
        if (trip == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trip);
    }

    @GetMapping("/retrieve-seats-available/{trip-id}")
    @Operation(summary = "Get available seats for a trip")
    public ResponseEntity<Integer> retrieveTripSeatsAvailable(@PathVariable("trip-id") Long tripId) {
        Integer seatsAvailable = tripService.retrieveTripSeatsAvailable(tripId);
        if (seatsAvailable == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(seatsAvailable);
    }
}
