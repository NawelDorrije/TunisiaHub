package org.example.backend_tunisiahub.Entities.Camping.Mappers;

import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.stereotype.Component;

/**
 * Manual mapper — keeps things explicit without MapStruct boilerplate.
 */
@Component
public class PaymentMapper {

    public PaymentDTO toDTO(Payment p) {
        if (p == null) return null;

        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setAmount(p.getAmount());
        dto.setDepositAmount(p.getDepositAmount());
        dto.setRemainingAmount(p.getRemainingAmount());
        dto.setMinimumDepositPercent(p.getMinimumDepositPercent());
        dto.setMethod(p.getMethod());
        dto.setRemainingPaymentMethod(p.getRemainingPaymentMethod());
        dto.setStatus(p.getStatus());
        dto.setTransactionRef(p.getTransactionRef());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setPaidAt(p.getPaidAt());
        dto.setQrCodeBase64(p.getQrCodeBase64());

        Reservation r = p.getReservation();
        if (r != null) {
            dto.setReservationId(r.getId());
            dto.setReservationSummary(buildSummary(r));

            User user = r.getUser();
            if (user != null) {
                dto.setClientId(user.getId());
                dto.setClientEmail(user.getEmail());
                dto.setClientName(user.getNom());
            }
        }

        return dto;
    }

    private String buildSummary(Reservation r) {
        if (r.getSpot() == null) return "Reservation #" + r.getId();
        var spot    = r.getSpot();
        var camping = spot.getCamping();
        return String.format("Reservation #%d | %s – %s | %s / %s",
                r.getId(),
                camping != null ? camping.getName() : "?",
                spot.getName(),
                r.getCheckIn(),
                r.getCheckOut());
    }
}