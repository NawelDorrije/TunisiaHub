package org.example.backend_tunisiahub.Repositories.SouvenirsShops;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"user", "shop"})
    Optional<Order> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findByShopIdOrderByCreatedAtDesc(Long shopId);

    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findByShopOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime createdAt);

    @EntityGraph(attributePaths = {"user", "shop"})
    List<Order> findByShopOwnerIdAndCreatedAtAfterOrderByCreatedAtDesc(Long ownerId, LocalDateTime createdAt);

    boolean existsByUserIdAndShopIdAndStatusIn(Long userId, Long shopId, List<OrderStatus> statuses);
}
