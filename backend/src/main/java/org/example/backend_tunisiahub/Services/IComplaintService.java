package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Complaint;

import java.util.List;

public interface IComplaintService {

    List<Complaint> retrieveAllComplaints();

    Complaint addComplaint(Complaint complaint, Long reservationId);

    Complaint updateComplaintStatus(Long complaintId, String status);
}
