package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}
