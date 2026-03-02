package org.example.backend_tunisiahub.carpooling.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.carpooling.dto.TripCreateRequest;
import org.example.backend_tunisiahub.carpooling.dto.TripResponse;
import org.example.backend_tunisiahub.carpooling.dto.TripUpdateRequest;
import org.example.backend_tunisiahub.carpooling.service.TripService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/carpooling/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest request,
                                                   HttpServletRequest httpRequest) {
        Long currentUserId = currentUserResolver.getUserId(httpRequest);
        ensureDriverRole(httpRequest);
        log.info("Create trip request received for driverId={}, departure={}, destination={}, departureDateTime={}",
            currentUserId,
            request.getDeparturePoint(),
            request.getDestination(),
            request.getDepartureDateTime());
        TripResponse response = tripService.createTrip(request, currentUserId);
        log.info("Create trip succeeded for driverId={}, tripId={}", currentUserId, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<TripResponse> searchTrips(@RequestParam(required = false) String departurePoint,
                                          @RequestParam(required = false) String destination,
                                          @RequestParam(required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripService.searchTrips(departurePoint, destination, date);
    }

    @GetMapping("/{id}")
    public TripResponse getTripById(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    @GetMapping("/me")
    public List<TripResponse> getMyTrips(HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureDriverRole(request);
        return tripService.getMyTrips(currentUserId);
    }

    @PatchMapping("/{id}/cancel")
    public TripResponse cancelTrip(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureDriverRole(request);
        return tripService.cancelTrip(id, currentUserId);
    }

    @PutMapping("/{id}")
    public TripResponse updateTrip(@PathVariable Long id,
                                   @Valid @RequestBody TripUpdateRequest payload,
                                   HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        ensureDriverRole(request);
        return tripService.updateTrip(id, currentUserId, payload);
    }

    private void ensureDriverRole(HttpServletRequest request) {
        String role = currentUserResolver.getRole(request);
        if (!"DRIVER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with DRIVER role can perform this action");
        }
    }
}
