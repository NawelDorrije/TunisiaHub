package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Complaint;
import org.example.backend_tunisiahub.Services.IComplaintService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final IComplaintService complaintService;

    @GetMapping
    public List<ComplaintView> getAllComplaints() {
        return complaintService.retrieveAllComplaints().stream().map(this::toView).toList();
    }

    @PostMapping
    public ComplaintView createComplaint(@RequestBody ComplaintCreateRequest request) {
        Complaint complaint = new Complaint();
        complaint.setDescription(request.description());
        complaint.setDate(request.date() != null ? request.date() : new Date());
        complaint.setReportedByUserId(request.reportedByUserId());
        complaint.setStatus("OPEN");

        Long reservationId = request.reservation() != null ? request.reservation().id() : null;
        return toView(complaintService.addComplaint(complaint, reservationId));
    }

    @PatchMapping("/{id}/status")
    public ComplaintView updateComplaintStatus(@PathVariable Long id, @RequestBody ComplaintStatusRequest request) {
        Complaint complaint = complaintService.updateComplaintStatus(id, request.status());
        return complaint != null ? toView(complaint) : null;
    }

    private ComplaintView toView(Complaint complaint) {
        return new ComplaintView(
            complaint.getId(),
            complaint.getDescription(),
            complaint.getDate(),
            complaint.getReportedByUserId(),
            complaint.getReservation() != null ? complaint.getReservation().getId() : null,
            complaint.getStatus()
        );
    }

    private record ComplaintCreateRequest(
        String description,
        Date date,
        String reportedByUserId,
        ReservationRef reservation
    ) {}

    private record ReservationRef(Long id) {}

    private record ComplaintStatusRequest(String status) {}

    private record ComplaintView(
        Long id,
        String description,
        Date date,
        String reportedByUserId,
        Long reservationId,
        String status
    ) {}
}
