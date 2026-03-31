package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.Optional;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
}
