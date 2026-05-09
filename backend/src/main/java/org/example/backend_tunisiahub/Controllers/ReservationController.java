package org.example.backend_tunisiahub.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.dto.AiReservationSuggestionResponse;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Services.AiRecommendationService;
import org.example.backend_tunisiahub.Services.ICampingReservationService;
import org.example.backend_tunisiahub.Services.ITripReservationService;
import org.example.backend_tunisiahub.Services.ReservationQuote;
import org.example.backend_tunisiahub.Services.ReservationServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;
    private final AiRecommendationService aiRecommendationService;
    private final ITripReservationService tripReservationService;
    private final ICampingReservationService campingReservationService;

    // Restaurant reservation endpoints
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

    @GetMapping("/ai-suggestions")
    public AiReservationSuggestionResponse getAiSuggestion(@RequestParam Long restaurantId,
                                                           @RequestParam LocalDate date) {
        return aiRecommendationService.suggestBestTime(restaurantId, date);
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

    // Camping reservation endpoints
    @PostMapping("/camping")
    public ResponseEntity<ReservationDTO> createCampingReservation(@Valid @RequestBody ReservationDTO dto) {
        return ResponseEntity.ok(campingReservationService.createReservation(dto));
    }

    @GetMapping("/camping/{id}")
    public ResponseEntity<ReservationDTO> getCampingReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(campingReservationService.getById(id));
    }

    @GetMapping("/camping")
    public ResponseEntity<List<ReservationDTO>> getAllCampingReservations() {
        return ResponseEntity.ok(campingReservationService.getAll());
    }

    @GetMapping("/camping/user/{userId}")
    public ResponseEntity<List<ReservationDTO>> getCampingReservationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(campingReservationService.getByUser(userId));
    }

    @GetMapping("/camping/spot/{spotId}")
    public ResponseEntity<List<ReservationDTO>> getCampingReservationsBySpot(@PathVariable Long spotId) {
        return ResponseEntity.ok(campingReservationService.getBySpot(spotId));
    }

    @GetMapping("/camping/status")
    public ResponseEntity<List<ReservationDTO>> getCampingReservationsByStatus(
            @RequestParam org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus status) {
        return ResponseEntity.ok(campingReservationService.getByStatus(status));
    }

    @PatchMapping("/camping/{id}/status")
    public ResponseEntity<ReservationDTO> updateCampingReservationStatus(
            @PathVariable Long id,
            @RequestParam org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus status) {
        return ResponseEntity.ok(campingReservationService.updateStatus(id, status));
    }

    @DeleteMapping("/camping/{id}/cancel")
    public ResponseEntity<Void> cancelCampingReservation(@PathVariable Long id) {
        campingReservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }

    // Carpooling reservation endpoints
    @GetMapping("/carpooling")
    public List<Reservation> getAllCarpoolingReservations() {
        return tripReservationService.retrieveAllReservations();
    }

    @GetMapping("/carpooling/user/{userId}")
    public List<Reservation> getCarpoolingReservationsByUserId(@PathVariable Long userId) {
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
    public Reservation getCarpoolingReservationById(@PathVariable Long id) {
        return tripReservationService.retrieveReservation(id);
    }

    @PostMapping("/carpooling")
    public ResponseEntity<Reservation> createCarpoolingReservation(@RequestBody Reservation reservation,
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
    public ResponseEntity<Reservation> updateCarpoolingReservation(@RequestBody Reservation reservation,
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
    public ResponseEntity<Reservation> approveCarpoolingReservation(@PathVariable Long id,
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
    public ResponseEntity<Reservation> rejectCarpoolingReservation(@PathVariable Long id,
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
