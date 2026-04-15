package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
            select case when count(oi) > 0 then true else false end
            from OrderItem oi
            where oi.order.user.id = :userId
              and oi.product.id = :productId
              and oi.order.status in :statuses
            """)
    boolean existsByUserAndProductAndOrderStatusIn(
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("statuses") List<OrderStatus> statuses
    );
}
