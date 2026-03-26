package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Services.Carpooling.ITripService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                       @RequestParam(required = false) Integer seatsRequired) {
        return tripService.retrieveAllTrips(departurePoint, destination, date, seatsRequired);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get public trip details by id")
    public Trip retrieveTrip(@PathVariable Long id) {
        return tripService.retrieveTrip(id);
    }
}
