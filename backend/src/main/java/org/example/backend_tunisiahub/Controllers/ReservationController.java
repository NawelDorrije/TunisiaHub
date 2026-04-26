package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Services.IReservationService;
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

    @GetMapping("/my")
    public List<Reservation> getMyReservations() {
        return reservationService.retrieveMyReservations();
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUser(@PathVariable Long userId) {
        return reservationService.retrieveReservationsByUser(userId);
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.retrieveReservation(id);
    }

    @PostMapping
    public Reservation createReservation(@RequestBody Reservation reservation) {
        return reservationService.addReservation(reservation);
    }

    @GetMapping("/restaurant")
    public List<Reservation> getRestaurantReservations(@RequestParam(required = false) Long restaurantId,
                                                       @RequestParam(required = false) ReservationStatus status) {
        return reservationService.retrieveRestaurantReservations(restaurantId, status);
    }

    @PutMapping
    public Reservation updateReservation(@RequestBody Reservation reservation) {
        return reservationService.modifyReservation(reservation);
    }

    @PatchMapping("/{id}/confirm")
    public Reservation confirmRestaurantReservation(@PathVariable Long id,
                                                    @RequestBody ConfirmReservationRequest request) {
        return reservationService.confirmRestaurantReservation(id, request.tableIds());
    }

    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }

    @PatchMapping("/{id}/complete")
    public Reservation completeReservation(@PathVariable Long id) {
        return reservationService.completeReservation(id);
    }

    @PostMapping("/checkin")
    public Reservation checkInReservation(@RequestParam String token) {
        return reservationService.checkInReservation(token);
    }

    @GetMapping(value = "/checkin-public", produces = "text/html")
    public String checkInPublic(@RequestParam String token) {
        try {
            Reservation res = reservationService.checkInReservation(token);
            return "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>" +
                   "<style>body{font-family:sans-serif;text-align:center;padding:50px 20px;background:#f8fafc;}" +
                   ".card{background:white;padding:30px;border-radius:15px;box-shadow:0 4px 6px rgba(0,0,0,0.1);max-width:400px;margin:auto;}" +
                   "h1{color:#22c55e;} .info{margin:20px 0;font-size:18px;color:#64748b;}" +
                   ".badge{background:#22c55e;color:white;padding:5px 15px;border-radius:20px;font-weight:bold;}" +
                   "</style></head><body>" +
                   "<div class='card'>" +
                   "<h1>CLIENT ARRIVED! </h1>" +
                   "<div class='info'>" +
                   "<p>Reservation ID: <b>#" + res.getId() + "</b></p>" +
                   "<p>Client: <b>" + (res.getUser() != null ? res.getUser().getNom() : "Guest") + "</b></p>" +
                   "<p>Restaurant: <b>" + res.getRestaurant().getName() + "</b></p>" +
                   "</div>" +
                   "<span class='badge'>STATUS: ARRIVED</span>" +
                   "</div>" +
                   "</body></html>";
        } catch (Exception e) {
            return "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>" +
                   "<style>body{font-family:sans-serif;text-align:center;padding:50px 20px;background:#fff5f5;}" +
                   ".card{background:white;padding:30px;border-radius:15px;box-shadow:0 4px 6px rgba(0,0,0,0.1);max-width:400px;margin:auto;}" +
                   "h1{color:#ef4444;}</style></head><body>" +
                   "<div class='card'>" +
                   "<h1>CHECK-IN FAILED ❌</h1>" +
                   "<p style='color:#64748b;'>" + (e.getMessage().contains("already") ? "This client is already checked in." : e.getMessage()) + "</p>" +
                   "</div>" +
                   "</body></html>";
        }
    }

    @GetMapping("/statuses")
    public ReservationStatus[] getReservationStatuses() {
        return ReservationStatus.values();
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }

    private record ConfirmReservationRequest(List<Long> tableIds) {}
}
