package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.List;
import java.util.Optional;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    Optional<Payment> findTopByOrderIdOrderByCreatedAtDesc(Long orderId);

    Optional<Payment> findTopByOrderIdAndStatusOrderByCreatedAtDesc(Long orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatusIn(Long orderId, List<PaymentStatus> statuses);
}
