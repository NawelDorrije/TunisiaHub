package org.example.backend_tunisiahub.Repositories.Event;

import org.example.backend_tunisiahub.Entities.Event.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
