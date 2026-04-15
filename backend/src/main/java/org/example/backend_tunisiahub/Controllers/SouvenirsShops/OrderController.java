package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CreateOrdersRequest;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.UpdateOrderStatusRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Services.SouvenirsShops.IOrderService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/souvenir-shops/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.retrieveAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.retrieveOrder(id);
    }

    @GetMapping("/me")
    public List<Order> getMyOrders() {
        return orderService.retrieveMyOrders();
    }

    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {
        return orderService.retrieveOrdersByUser(userId);
    }

    @GetMapping("/shop/{shopId}")
    public List<Order> getOrdersByShop(@PathVariable Long shopId) {
        return orderService.retrieveOrdersByShop(shopId);
    }

    @GetMapping("/{id}/items")
    public List<OrderItem> getOrderItems(@PathVariable Long id) {
        return orderService.retrieveOrderItems(id);
    }

    @GetMapping("/{id}/payments")
    public List<Payment> getOrderPayments(@PathVariable Long id) {
        return orderService.retrieveOrderPayments(id);
    }

    @PostMapping
    public List<Order> createOrders(@RequestBody CreateOrdersRequest request) {
        return orderService.addOrdersFromCart(request == null ? null : request.getItems());
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(
                id,
                request == null ? null : request.getStatus(),
                request == null ? null : request.getGenerateAiMessage()
        );
    }

    @GetMapping("/issues")
    public List<String> detectOrderIssues() {
        return orderService.detectOrderIssues();
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.cancelOrderByClient(id);
    }
}
