package org.example.backend_tunisiahub.Controllers;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Services.IReservationService;
import org.example.backend_tunisiahub.Services.ITripReservationService;
import org.example.backend_tunisiahub.Services.ReservationQuote;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final IReservationService campingReservationService;
    private final ITripReservationService tripReservationService;

    public ReservationController(IReservationService campingReservationService,
                                 ITripReservationService tripReservationService) {
        this.campingReservationService = campingReservationService;
        this.tripReservationService = tripReservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationDTO> create(@Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.ok(campingReservationService.createReservation(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(campingReservationService.getById(id));
    }

    @GetMapping("/carpooling")
    public List<Reservation> getAllReservations() {
        return tripReservationService.retrieveAllReservations();
    }

    @GetMapping("/carpooling/user/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
        return tripReservationService.retrieveReservationsByUserId(userId);
    }

    @GetMapping("/carpooling/trip/{tripId}")
    public ResponseEntity<List<Reservation>> getReservationsByTripId(@PathVariable Long tripId,
                                                                     HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(tripReservationService.retrieveReservationsByTripId(tripId, currentUserId));
    }

    @GetMapping("/carpooling/quote")
    public ReservationQuote getTripReservationQuote(
            @RequestParam Long tripId,
            @RequestParam(defaultValue = "1") Integer seats
    ) {
        return tripReservationService.calculateTripQuote(tripId, seats);
    }

    @GetMapping("/carpooling/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return tripReservationService.retrieveReservation(id);
    }

    @PostMapping("/carpooling")
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation savedReservation = tripReservationService.addReservation(reservation, currentUserId);
        if (savedReservation == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedReservation);
    }

    @PutMapping("/carpooling")
    public ResponseEntity<Reservation> updateReservation(@RequestBody Reservation reservation,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation savedReservation = tripReservationService.modifyReservation(reservation, currentUserId);
        if (savedReservation == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedReservation);
    }

    @PutMapping("/carpooling/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable Long id,
                                                          HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation reservation = tripReservationService.approveReservation(id, currentUserId);
        if (reservation == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/carpooling/{id}/reject")
    public ResponseEntity<Reservation> rejectReservation(@PathVariable Long id,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation reservation = tripReservationService.rejectReservation(id, currentUserId);
        if (reservation == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAll() {
        return ResponseEntity.ok(campingReservationService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(campingReservationService.getByUser(userId));
    }

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<List<ReservationDTO>> getBySpot(@PathVariable Long spotId) {
        return ResponseEntity.ok(campingReservationService.getBySpot(spotId));
    }

    @GetMapping("/status")
    public ResponseEntity<List<ReservationDTO>> getByStatus(@RequestParam ReservationStatus status) {
        return ResponseEntity.ok(campingReservationService.getByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(campingReservationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        campingReservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
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
