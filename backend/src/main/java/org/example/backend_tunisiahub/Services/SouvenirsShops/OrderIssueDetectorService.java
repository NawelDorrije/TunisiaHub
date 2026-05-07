package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderIssueDetectorService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<String> detectIssuesForOrders(List<Order> scopedOrders) {
        List<String> issues = new ArrayList<>();
        issues.addAll(detectFrequentUserCancellations(scopedOrders));
        issues.addAll(detectHighProductCancellationRate(scopedOrders));
        return issues;
    }

    public List<Order> loadRecentOrdersForAdmin() {
        LocalDateTime since30Days = LocalDateTime.now().minusDays(30);
        return orderRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since30Days);
    }

    public List<Order> loadRecentOrdersForOwner(Long ownerId) {
        LocalDateTime since30Days = LocalDateTime.now().minusDays(30);
        return orderRepository.findByShopOwnerIdAndCreatedAtAfterOrderByCreatedAtDesc(ownerId, since30Days);
    }

    private List<String> detectFrequentUserCancellations(List<Order> scopedOrders) {
        LocalDateTime since24Hours = LocalDateTime.now().minusHours(24);
        Map<Long, Integer> cancelledByUser = new HashMap<>();
        Map<Long, String> userDisplay = new HashMap<>();

        for (Order order : scopedOrders) {
            if (order.getCreatedAt() == null || order.getCreatedAt().isBefore(since24Hours)) {
                continue;
            }
            if (order.getStatus() != OrderStatus.CANCELLED) {
                continue;
            }
            if (order.getUser() == null || order.getUser().getId() == null) {
                continue;
            }

            Long userId = order.getUser().getId();
            cancelledByUser.merge(userId, 1, Integer::sum);
            String label = order.getUser().getPrenom() + " " + order.getUser().getNom()
                    + " (id=" + userId + ")";
            userDisplay.put(userId, label.trim());
        }

        List<String> issues = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cancelledByUser.entrySet()) {
            if (entry.getValue() >= 3) {
                issues.add(entry.getValue() + " orders from user "
                        + userDisplay.get(entry.getKey())
                        + " cancelled in last 24h - possible fraud alert");
            }
        }
        return issues;
    }

    private List<String> detectHighProductCancellationRate(List<Order> scopedOrders) {
        Map<Long, ProductOrderStats> statsByProduct = new HashMap<>();

        for (Order order : scopedOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    continue;
                }
                Long productId = item.getProduct().getId();
                ProductOrderStats stats = statsByProduct.computeIfAbsent(
                        productId,
                        ignored -> new ProductOrderStats(item.getProduct().getName())
                );
                stats.total++;
                if (order.getStatus() == OrderStatus.CANCELLED) {
                    stats.cancelled++;
                }
            }
        }

        List<String> issues = new ArrayList<>();
        for (Map.Entry<Long, ProductOrderStats> entry : statsByProduct.entrySet()) {
            ProductOrderStats stats = entry.getValue();
            if (stats.total < 5) {
                continue;
            }
            double ratio = (double) stats.cancelled / (double) stats.total;
            if (ratio >= 0.80d) {
                int pct = (int) Math.round(ratio * 100);
                issues.add("Product " + stats.name + " (id=" + entry.getKey() + ") has "
                        + pct + "% cancellation rate in recent orders - possible stock or quality issue");
            }
        }
        return issues;
    }

    private static class ProductOrderStats {
        private final String name;
        private int total;
        private int cancelled;

        private ProductOrderStats(String name) {
            this.name = name;
        }
    }
}
