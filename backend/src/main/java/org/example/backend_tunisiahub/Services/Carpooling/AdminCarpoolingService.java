package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Complaint;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.Review;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Carpooling.CarpoolingReviewRepository;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.ComplaintRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class AdminCarpoolingService implements IAdminCarpoolingService {

    private TripRepository tripRepository;
    private ReservationRepository reservationRepository;
    private CarpoolingReviewRepository reviewRepository;
    private ComplaintRepository complaintRepository;
    private GeminiComplaintAnalysisService geminiComplaintAnalysisService;

    @Override
    public List<Trip> retrieveTrips(String status,
                                    String departure,
                                    String destination,
                                    Long driverId,
                                    LocalDate dateFrom,
                                    LocalDate dateTo) {
        LocalDateTime start = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime end = dateTo != null ? dateTo.plusDays(1).atStartOfDay() : null;

        return tripRepository.findAdminTrips(
                cleanTripStatus(status),
                clean(departure),
                clean(destination),
                driverId,
                start,
                end
        );
    }

    @Override
    public Trip cancelTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            return null;
        }

        trip.setStatus("canceled");
        cancelTripReservations(tripId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip reactivateTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            return null;
        }

        trip.setStatus("scheduled");
        confirmTripReservations(tripId);
        return tripRepository.save(trip);
    }

    @Override
    public void removeTrip(Long tripId) {
        if (reservationRepository.countByTripId(tripId) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This trip has reservations. Cancel it instead of removing it."
            );
        }
        tripRepository.deleteById(tripId);
    }

    @Override
    public List<Reservation> retrieveReservations(Long tripId, String status) {
        return reservationRepository.findAdminTripReservations(tripId, clean(status));
    }

    @Override
    public List<AdminDriverView> retrieveDrivers() {
        return tripRepository.findCarpoolingDrivers()
                .stream()
                .map(this::buildDriverView)
                .toList();
    }

    @Override
    public List<Complaint> retrieveComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint analyzeComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null) {
            return null;
        }

        ComplaintAiAnalysis analysis = geminiComplaintAnalysisService.analyze(complaint.getDescription());
        complaint.setAiSummary(analysis.summary());
        complaint.setAiKeywords(analysis.keywords());
        complaint.setAiSolutions(analysis.solutions());
        return complaintRepository.save(complaint);
    }

    @Override
    public List<Review> retrieveBadReviews() {
        return reviewRepository.findBadReviews(2);
    }

    private AdminDriverView buildDriverView(User driver) {
        List<Trip> trips = tripRepository.findByDriverIdOrderByDepartureDateTimeDesc(driver.getId());
        long reservationsCount = reservationRepository.countByTripDriverId(driver.getId());
        long canceledReservationsCount = reservationRepository.countCanceledByTripDriverId(driver.getId());
        double cancellationRate = reservationsCount > 0
                ? (canceledReservationsCount * 100.0) / reservationsCount
                : 0;
        Double averageRating = reviewRepository.findAverageRatingByDriverId(driver.getId());
        long reviewsCount = reviewRepository.countByReservation_Trip_Driver_Id(driver.getId());
        long reportedIssues = complaintRepository.countByDriverId(driver.getId());

        return new AdminDriverView(
                driver,
                trips,
                reservationsCount,
                canceledReservationsCount,
                cancellationRate,
                averageRating != null ? averageRating : 0,
                reviewsCount,
                reportedIssues
        );
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void cancelTripReservations(Long tripId) {
        List<Reservation> reservations = reservationRepository.findByTripId(tripId);
        for (Reservation reservation : reservations) {
            if (!isCanceled(reservation.getStatus())) {
                reservation.setStatus("CANCELED");
            }
        }
        reservationRepository.saveAll(reservations);
    }

    private void confirmTripReservations(Long tripId) {
        List<Reservation> reservations = reservationRepository.findByTripId(tripId);
        for (Reservation reservation : reservations) {
            if (isCanceled(reservation.getStatus())) {
                reservation.setStatus("CONFIRMED");
            }
        }
        reservationRepository.saveAll(reservations);
    }

    private boolean isCanceled(String status) {
        return status != null &&
                (status.equalsIgnoreCase("CANCELED") || status.equalsIgnoreCase("CANCELLED"));
    }

    private String cleanTripStatus(String value) {
        String status = clean(value);
        if (status == null) {
            return null;
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return "scheduled";
        }
        if ("CANCELED".equalsIgnoreCase(status)) {
            return "canceled";
        }
        return status;
    }
}
