package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Services.IReservationEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationEventController {

    private final IReservationEventService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.retrieveAllReservations();
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.retrieveReservation(id);
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.addReservation(reservation);
    }

    @PutMapping
    public Reservation updateReservation(@RequestBody Reservation reservation) {
        return reservationService.modifyReservation(reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }

    @PostMapping("/reserve")
    public Reservation reserveEvent(
            @RequestParam Long userId,
            @RequestParam Long eventId
    ) {
        return reservationService.reserveEvent(userId, eventId);
    }
    @PostMapping("/create-pending")
    public Reservation createPending(
            @RequestParam Long userId,
            @RequestParam Long eventId
    ) {
        return reservationService.createPendingReservation(userId, eventId);
    }

    @PostMapping("/confirm/{id}")
    public Reservation confirm(@PathVariable Long id) {
        return reservationService.confirmReservation(id);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    // ✅ FIX IMPORTANT
    @GetMapping("/user/{userId}/event/{eventId}")
    public ResponseEntity<?> getReservation(@PathVariable Long userId,
                                            @PathVariable Long eventId) {

        Reservation res = reservationService.findByUserAndEvent(userId, eventId);

        if (res == null) {
            return ResponseEntity.notFound().build();
        }




        return ResponseEntity.ok(res);
    }

}
