package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Services.IReservationService;
import org.example.backend_tunisiahub.Services.ReservationQuote;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.retrieveAllReservations();
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUserId(@PathVariable Long userId) {
        return reservationService.retrieveReservationsByUserId(userId);
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<Reservation>> getReservationsByTripId(@PathVariable Long tripId,
                                                                     HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservationService.retrieveReservationsByTripId(tripId, currentUserId));
    }

    @GetMapping("/quote")
    public ReservationQuote getTripReservationQuote(
            @RequestParam Long tripId,
            @RequestParam(defaultValue = "1") Integer seats
    ) {
        return reservationService.calculateTripQuote(tripId, seats);
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.retrieveReservation(id);
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(@RequestBody Reservation reservation,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation savedReservation = reservationService.addReservation(reservation, currentUserId);
        if (savedReservation == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedReservation);
    }

    @PutMapping
    public ResponseEntity<Reservation> updateReservation(@RequestBody Reservation reservation,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation savedReservation = reservationService.modifyReservation(reservation, currentUserId);
        if (savedReservation == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedReservation);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(@PathVariable Long id,
                                                          HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation reservation = reservationService.approveReservation(id, currentUserId);
        if (reservation == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservation);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Reservation> rejectReservation(@PathVariable Long id,
                                                         HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        Reservation reservation = reservationService.rejectReservation(id, currentUserId);
        if (reservation == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
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
