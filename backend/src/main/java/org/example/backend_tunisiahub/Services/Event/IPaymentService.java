package org.example.backend_tunisiahub.Services.Event;

import org.example.backend_tunisiahub.Entities.Event.Payment;

import java.util.List;

public interface IPaymentService {

    List<Payment> retrieveAllPayments();

    Payment retrievePayment(Long id);

    Payment addPayment(Payment payment);

    void deletePayment(Long id);

    Payment modifyPayment(Payment payment);

    Payment pay(Long reservationId, double amount);
}