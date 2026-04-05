package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
    List<Payment> findByStatus(PaymentStatus status);
}