package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Services.Accommodation.IAccommodationReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accommodation-reservations")
@RequiredArgsConstructor
public class AccommodationReservationController {

    private final IAccommodationReservationService reservationService;

    @PostMapping("/add/{accommodationId}")
    public ResponseEntity<?> addReservation(
            @PathVariable Long accommodationId,
            @RequestBody Reservation reservation,
            @AuthenticationPrincipal String email) {

        if (accommodationId <= 0) return ResponseEntity.badRequest().body("Invalid accommodation ID");
        if (reservation.getStartDate() == null || reservation.getEndDate() == null)
            return ResponseEntity.badRequest().body("Start and end dates are required");
        if (!reservation.getEndDate().after(reservation.getStartDate()))
            return ResponseEntity.badRequest().body("End date must be after start date");

        Reservation saved = reservationService.addAccommodationReservation(accommodationId, reservation, email);
        return ResponseEntity.status(201).body(toReservationResponseEntity(saved));
    }


    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<List<Map<String, Object>>> getReservationsByAccommodation(@PathVariable Long accommodationId) {
        return ResponseEntity.ok(
                reservationService.getReservationsByAccommodation(accommodationId)
                        .stream()
                        .map(this::toReservationResponseEntity)
                        .toList()
        );
    }

    @GetMapping("/check-availability/{accommodationId}")
    public ResponseEntity<?> checkAvailability(
            @PathVariable Long accommodationId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        boolean available = reservationService.isAccommodationAvailable(accommodationId, startDate, endDate);
        return ResponseEntity.ok(available);
    }

    @PutMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId) {
        Reservation cancelled = reservationService.cancelReservation(reservationId);
        if (cancelled == null) return ResponseEntity.status(404).body("Reservation not found");
        return ResponseEntity.ok(toReservationResponseEntity(cancelled));
    }

    @GetMapping("/reserved-dates/{accommodationId}")
    public ResponseEntity<List<Map<String, Date>>> getReservedDates(@PathVariable Long accommodationId) {
        List<Reservation> reservations = reservationService.getReservationsByAccommodation(accommodationId);
        List<Map<String, Date>> reservedDates = reservations.stream()
                .filter(r -> r.getStatus().equals("CONFIRMED"))
                .map(r -> {
                    Map<String, Date> range = new HashMap<>();
                    range.put("startDate", r.getStartDate());
                    range.put("endDate", r.getEndDate());
                    return range;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(reservedDates);
    }
    @PutMapping("/edit/{reservationId}")
    public ResponseEntity<?> editReservation(
            @PathVariable Long reservationId,
            @RequestBody Reservation updated,
            @AuthenticationPrincipal String email) {

        if (updated.getStartDate() == null || updated.getEndDate() == null)
            return ResponseEntity.badRequest().body("Dates are required");
        if (!updated.getEndDate().after(updated.getStartDate()))
            return ResponseEntity.badRequest().body("End date must be after start date");

        Reservation result = reservationService.editReservation(reservationId, updated, email);
        if (result == null)
            return ResponseEntity.status(409).body("Dates not available or reservation not found");
        return ResponseEntity.ok(toReservationResponseEntity(result));
    }
    @GetMapping("/my-reservations")
    public ResponseEntity<List<Map<String, Object>>> getMyReservations(
            @AuthenticationPrincipal String email) {
        List<Reservation> reservations = reservationService.getReservationsByUser(email);
        return ResponseEntity.ok(
                reservations.stream()
                        .map(this::toReservationResponseEntity)
                        .toList()
        );
    }
    @GetMapping("/statistics")
    public ResponseEntity<AccommodationStatsDTO> getStatistics() {
        return ResponseEntity.ok(reservationService.getStatistics());
    }

    private Map<String, Object> toReservationResponseEntity(Reservation reservation) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", reservation.getId());
        response.put("status", reservation.getStatus());
        response.put("type", reservation.getType());
        response.put("startDate", reservation.getStartDate());
        response.put("endDate", reservation.getEndDate());
        response.put("totalPrice", reservation.getTotalPrice());
        response.put("notes", reservation.getNotes());
        response.put("createdAt", reservation.getCreatedAt());
        response.put("updatedAt", reservation.getUpdatedAt());
        response.put("userId", reservation.getUser() != null ? reservation.getUser().getId() : null);
        response.put("accommodationId", reservation.getAccommodation() != null ? reservation.getAccommodation().getId() : null);
        response.put("accommodationTitle", reservation.getAccommodation() != null ? reservation.getAccommodation().getTitle() : null);
        return response;
    }

}
