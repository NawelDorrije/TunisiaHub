package org.example.backend_tunisiahub.Controllers.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Complaint;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Services.Carpooling.AdminDriverView;
import org.example.backend_tunisiahub.Services.Carpooling.IAdminCarpoolingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/carpooling")
public class AdminCarpoolingController {

  IAdminCarpoolingService adminCarpoolingService;

  @GetMapping("/trips")
  public List<Trip> getTrips(@RequestParam(required = false) String status,
                             @RequestParam(required = false) String departure,
                             @RequestParam(required = false) String destination,
                             @RequestParam(required = false) Long driverId,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
    return adminCarpoolingService.retrieveTrips(status, departure, destination, driverId, dateFrom, dateTo);
  }

  @PutMapping("/trips/{id}/cancel")
  public ResponseEntity<Trip> cancelTrip(@PathVariable Long id) {
    Trip trip = adminCarpoolingService.cancelTrip(id);
    if (trip == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(trip);
  }

  @PutMapping("/trips/{id}/reactivate")
  public ResponseEntity<Trip> reactivateTrip(@PathVariable Long id) {
    Trip trip = adminCarpoolingService.reactivateTrip(id);
    if (trip == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(trip);
  }

  @DeleteMapping("/trips/{id}")
  public ResponseEntity<Map<String, String>> removeTrip(@PathVariable Long id) {
    try {
      adminCarpoolingService.removeTrip(id);
      return ResponseEntity.ok(Map.of("message", "Trip removed successfully."));
    } catch (ResponseStatusException exception) {
      return ResponseEntity
        .status(exception.getStatusCode())
        .body(Map.of("message", exception.getReason()));
    }
  }

  @GetMapping("/reservations")
  public List<Reservation> getReservations(@RequestParam(required = false) Long tripId,
                                           @RequestParam(required = false) String status) {
    return adminCarpoolingService.retrieveReservations(tripId, status);
  }

  @GetMapping("/drivers")
  public List<AdminDriverView> getDrivers() {
    return adminCarpoolingService.retrieveDrivers();
  }

  @GetMapping("/complaints")
  public List<ComplaintReportView> getComplaints() {
    return adminCarpoolingService.retrieveComplaints().stream().map(this::toComplaintReportView).toList();
  }

  @PutMapping("/complaints/{id}/analyze")
  public ResponseEntity<ComplaintReportView> analyzeComplaint(@PathVariable Long id) {
    Complaint complaint = adminCarpoolingService.analyzeComplaint(id);
    if (complaint == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(toComplaintReportView(complaint));
  }

  @GetMapping("/bad-reviews")
  public List<BadReviewView> getBadReviews() {
    return adminCarpoolingService.retrieveBadReviews().stream().map(this::toBadReviewView).toList();
  }

  private ComplaintReportView toComplaintReportView(Complaint complaint) {
    Reservation reservation = complaint.getReservation();
    Trip trip = reservation != null ? reservation.getTrip() : null;

    return new ComplaintReportView(
      complaint.getId(),
      complaint.getDescription(),
      complaint.getDate(),
      complaint.getReportedByUserId(),
      reservation != null ? reservation.getId() : null,
      trip != null ? trip.getId() : null,
      trip != null ? trip.getDeparture() : null,
      trip != null ? trip.getDestination() : null,
      complaint.getStatus(),
      complaint.getAiSummary(),
      complaint.getAiKeywords(),
      complaint.getAiSolutions()
    );
  }

  private BadReviewView toBadReviewView(Review review) {
    Reservation reservation = review.getReservation();
    Trip trip = reservation != null ? reservation.getTrip() : null;

    return new BadReviewView(
      review.getId(),
      review.getComment(),
      review.getRating(),
      review.getDate(),
      reservation != null ? reservation.getId() : null,
      trip != null ? trip.getId() : null,
      trip != null ? trip.getDeparture() : null,
      trip != null ? trip.getDestination() : null,
      trip != null && trip.getDriver() != null ? trip.getDriver().getId() : null,
      trip != null && trip.getDriver() != null
        ? ((trip.getDriver().getNom() != null ? trip.getDriver().getNom() : "") + " "
        + (trip.getDriver().getPrenom() != null ? trip.getDriver().getPrenom() : "")).trim()
        : null
    );
  }

  private record ComplaintReportView(
    Long id,
    String description,
    java.util.Date date,
    String reportedByUserId,
    Long reservationId,
    Long tripId,
    String departure,
    String destination,
    String status,
    String aiSummary,
    String aiKeywords,
    String aiSolutions
  ) {}

  private record BadReviewView(
    Long id,
    String comment,
    Integer rating,
    java.util.Date date,
    Long reservationId,
    Long tripId,
    String departure,
    String destination,
    Long driverId,
    String driverName
  ) {}
}
