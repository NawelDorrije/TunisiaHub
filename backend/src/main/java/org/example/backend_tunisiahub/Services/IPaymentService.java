package org.example.backend_tunisiahub.Services;


import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;

import java.util.List;

public interface IPaymentService {
    PaymentDTO processPayment(Long reservationId, PaymentMethod method);
    PaymentDTO getByReservationId(Long reservationId);
    PaymentDTO getById(Long id);
    List<PaymentDTO> getAll();
    PaymentDTO refund(Long paymentId);
}