package org.example.backend_tunisiahub.Entities.Camping.Mappers;


import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDTO(Payment p) {
        if (p == null) return null;

        Reservation r = p.getReservation();

        return PaymentDTO.builder()
                .id(p.getId())
                .reservationId(r.getId())
                .clientId(r.getUser().getId())               // ← Payment → Reservation → User
                .reservationSummary(
                        "Spot: " + r.getSpot().getName()
                                + " | " + r.getCheckIn() + " → " + r.getCheckOut()
                )
                .amount(p.getAmount())
                .method(p.getMethod())
                .status(p.getStatus())
                .transactionRef(p.getTransactionRef())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .build();
    }
}