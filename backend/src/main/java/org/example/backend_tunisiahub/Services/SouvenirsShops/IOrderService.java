package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;

public interface IOrderService {

    List<Order> retrieveAllOrders();

    Order retrieveOrder(Long id);

    List<Order> retrieveOrdersByUser(Long userId);

    List<Order> retrieveOrdersByShop(Long shopId);

    List<OrderItem> retrieveOrderItems(Long orderId);

    Payment retrieveOrderPayment(Long orderId);

    Order addOrder(Order order);

    void deleteOrder(Long id);

    Order modifyOrder(Order order);
}
