package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Payment;
import org.example.backend_tunisiahub.Entities.Event.PaymentMethod;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Repositories.Event.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Services.Event.IPaymentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;


    @Override
    public List<Payment> retrieveAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment retrievePayment(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public Payment addPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public Payment modifyPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Payment pay(Long reservationId, double amount) {

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setReservation(reservation);
        payment.setStatus("SUCCESS");

        // ✅ CONFIRM RESERVATION
        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);

        return paymentRepository.save(payment);
    }
}