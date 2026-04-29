package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Complaint;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.Review;

import java.time.LocalDate;
import java.util.List;

public interface IAdminCarpoolingService {

    List<Trip> retrieveTrips(String status,
                             String departure,
                             String destination,
                             Long driverId,
                             LocalDate dateFrom,
                             LocalDate dateTo);

    Trip cancelTrip(Long tripId);

    Trip reactivateTrip(Long tripId);

    void removeTrip(Long tripId);

    List<Reservation> retrieveReservations(Long tripId, String status);

    List<AdminDriverView> retrieveDrivers();

    List<Complaint> retrieveComplaints();

    Complaint analyzeComplaint(Long complaintId);

    List<Review> retrieveBadReviews();
}
