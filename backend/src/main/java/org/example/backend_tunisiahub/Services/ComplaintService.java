package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Complaint;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Repositories.ComplaintRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComplaintService implements IComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ReservationRestaurantRepository reservationRepository;

    @Override
    public List<Complaint> retrieveAllComplaints() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint addComplaint(Complaint complaint, Long reservationId) {
        if (complaint.getStatus() == null || complaint.getStatus().isBlank()) {
            complaint.setStatus("OPEN");
        }

        if (reservationId != null) {
            ReservationRestaurant reservation = reservationRepository.findById(reservationId).orElse(null);
            complaint.setReservation(reservation);
        }

        return complaintRepository.save(complaint);
    }

    @Override
    public Complaint updateComplaintStatus(Long complaintId, String status) {
        Complaint complaint = complaintRepository.findById(complaintId).orElse(null);
        if (complaint == null) {
            return null;
        }

        complaint.setStatus(status);
        return complaintRepository.save(complaint);
    }
}
