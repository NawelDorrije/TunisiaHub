package org.example.backend_tunisiahub.carpooling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.example.backend_tunisiahub.carpooling.service.ITripService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/carpooling/trips")
@RequiredArgsConstructor
@Tag(name = "Public Trips")
public class PublicTripController {

    private final ITripService tripService;

    @GetMapping
    @Operation(summary = "Search public scheduled trips")
    public Page<PublicTripView> searchTrips(@RequestParam(required = false) String departurePoint,
                                            @RequestParam(required = false) String destination,
                                            @RequestParam(required = false)
                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                            @RequestParam(required = false) Integer seatsRequired,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return tripService.searchPublicTrips(
                departurePoint,
                destination,
                date,
                seatsRequired,
                PageRequest.of(page, size)
        ).map(this::toPublicView);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public trip details by id")
    public PublicTripView getTripById(@PathVariable Long id) {
        return toPublicView(tripService.getPublicTripById(id));
    }

    private PublicTripView toPublicView(Trip trip) {
        return new PublicTripView(
                trip.getId(),
                trip.getDeparturePoint(),
                trip.getDestination(),
            trip.getDepartureDateTime(),
                trip.getPrice(),
                trip.getSeatsTotal(),
                trip.getSeatsAvailable(),
                trip.getStatus()
        );
    }

    private record PublicTripView(
            Long id,
            String departurePoint,
            String destination,
            LocalDateTime departureDateTime,
            BigDecimal price,
            int seatsTotal,
            int seatsAvailable,
            String status
    ) {
    }
}
