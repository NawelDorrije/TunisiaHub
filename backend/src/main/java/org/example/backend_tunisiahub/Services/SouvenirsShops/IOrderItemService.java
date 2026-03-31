package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;

public interface IOrderItemService {

    List<OrderItem> retrieveAllOrderItems();

    OrderItem retrieveOrderItem(Long id);

    List<OrderItem> retrieveOrderItemsByOrder(Long orderId);

    OrderItem addOrderItem(OrderItem orderItem);

    void deleteOrderItem(Long id);

    OrderItem modifyOrderItem(OrderItem orderItem);
}
