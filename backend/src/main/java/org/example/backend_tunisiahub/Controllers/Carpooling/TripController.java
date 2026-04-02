package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Services.Carpooling.ITripService;
import org.example.backend_tunisiahub.Services.Carpooling.RouteSuggestionService;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver/trips")
@AllArgsConstructor
@Tag(name = "Driver Trips")
public class TripController {

    ITripService tripService;
    RouteSuggestionService routeSuggestionService;
    CurrentUserResolver currentUserResolver;

    @PostMapping
    @Operation(summary = "Publish a trip")
    public Trip addTrip(@RequestBody Trip request, HttpServletRequest httpRequest) {
        Long currentUserId = currentUserResolver.getUserId(httpRequest);
        return tripService.addTrip(request, currentUserId);
    }

    @GetMapping
    @Operation(summary = "List current driver trips")
    public List<Trip> retrieveMyTrips(HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return tripService.retrieveMyTrips(currentUserId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get current driver trip details")
    public Trip retrieveTrip(@PathVariable Long id) {
        return tripService.retrieveTrip(id);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel trip")
    public Trip cancelTrip(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return tripService.cancelTrip(id, currentUserId);
    }

    @PutMapping("/{id}/available")
    @Operation(summary = "Make trip available again")
    public Trip makeTripAvailable(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return tripService.makeTripAvailable(id, currentUserId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trip")
    public Trip modifyTrip(@PathVariable Long id,
                           @RequestBody Trip payload,
                           HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return tripService.modifyTrip(id, payload, currentUserId);
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
}
