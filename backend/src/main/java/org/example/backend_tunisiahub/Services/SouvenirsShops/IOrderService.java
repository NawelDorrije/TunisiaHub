package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CartItemRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;

public interface IOrderService {

    List<Order> retrieveAllOrders();

    Order retrieveOrder(Long id);

    List<Order> retrieveMyOrders();

    List<Order> retrieveOrdersByUser(Long userId);

    List<Order> retrieveOrdersByShop(Long shopId);

    List<OrderItem> retrieveOrderItems(Long orderId);

    List<Payment> retrieveOrderPayments(Long orderId);

    List<Order> addOrdersFromCart(List<CartItemRequest> cartItems);

    void cancelOrderByClient(Long id);

    Order updateOrderStatus(Long id, OrderStatus newStatus, Boolean generateAiMessage);

    List<String> detectOrderIssues();
}
