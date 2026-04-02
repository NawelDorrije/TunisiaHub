package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderItemService implements IOrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<OrderItem> retrieveAllOrderItems() {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can view all order items");
        }
        return orderItemRepository.findAll();
    }

    @Override
    public OrderItem retrieveOrderItem(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));
        assertCanAccessOrder(orderItem.getOrder());
        return orderItem;
    }

    @Override
    public List<OrderItem> retrieveOrderItemsByOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        assertCanAccessOrder(order);
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public OrderItem addOrderItem(OrderItem orderItem) {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can manage order items directly");
        }
        if (orderItem.getOrder() == null || orderItem.getOrder().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order item must reference an order id");
        }
        if (orderItem.getProduct() == null || orderItem.getProduct().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order item must reference a product id");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order item quantity must be greater than 0");
        }

        Order order = orderRepository.findById(orderItem.getOrder().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        ensurePending(order);

        Product product = productRepository.findById(orderItem.getProduct().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.getShop().getId().equals(order.getShop().getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product must belong to the same shop as the order");
        }

        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setUnitPrice(product.getPrice());
        OrderItem saved = orderItemRepository.save(orderItem);
        recalculateOrderTotal(order.getId());
        return saved;
    }

    @Override
    public void deleteOrderItem(Long id) {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can manage order items directly");
        }
        OrderItem existing = orderItemRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));
        ensurePending(existing.getOrder());
        orderItemRepository.deleteById(id);
        recalculateOrderTotal(existing.getOrder().getId());
    }

    @Override
    public OrderItem modifyOrderItem(OrderItem orderItem) {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can manage order items directly");
        }
        if (orderItem.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order item id is required");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order item quantity must be greater than 0");
        }

        OrderItem existing = orderItemRepository.findById(orderItem.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));
        ensurePending(existing.getOrder());

        existing.setQuantity(orderItem.getQuantity());
        OrderItem saved = orderItemRepository.save(existing);
        recalculateOrderTotal(existing.getOrder().getId());
        return saved;
    }

    private void ensurePending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order items can only be modified for PENDING orders");
        }
    }

    private void assertCanAccessOrder(Order order) {
        if (isAdmin()) {
            return;
        }
        if (isClient() && order.getUser().getId().equals(getCurrentUser().getId())) {
            return;
        }
        if (isOwner() && order.getShop().getOwner().getId().equals(getCurrentUser().getId())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to access this order");
    }

    private void recalculateOrderTotal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        orderRepository.save(order);
    }

    private User getCurrentUser() {
        String email = getCurrentEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }
        return user;
    }

    private String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        return authentication.getName();
    }

    private boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (roleName.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    private boolean isOwner() {
        return hasRole("ROLE_OWNER");
    }

    private boolean isClient() {
        return hasRole("ROLE_CLIENT");
    }
}
