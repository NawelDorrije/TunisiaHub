package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CartItemRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.PaymentStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Override
    public List<Order> retrieveAllOrders() {
        if (isAdmin()) {
            return orderRepository.findAll();
        }
        if (isOwner()) {
            return orderRepository.findByShopOwnerIdOrderByCreatedAtDesc(getCurrentUser().getId());
        }
        if (isClient()) {
            return retrieveMyOrders();
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to view orders");
    }

    @Override
    public Order retrieveOrder(Long id) {
        Order order = findOrder(id);
        assertCanAccessOrder(order);
        return order;
    }

    @Override
    public List<Order> retrieveMyOrders() {
        if (!isClient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients can view their own orders");
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(getCurrentUser().getId());
    }

    @Override
    public List<Order> retrieveOrdersByUser(Long userId) {
        if (!isClient() && !isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients can view user orders");
        }
        if (!isAdmin() && !getCurrentUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only view your own orders");
        }
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Order> retrieveOrdersByShop(Long shopId) {
        if (!isOwner() && !isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only owners can view shop orders");
        }
        Shop shop = findShop(shopId);
        assertOwnerOfShopOrAdmin(shop);
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }

    @Override
    public List<OrderItem> retrieveOrderItems(Long orderId) {
        Order order = findOrder(orderId);
        assertCanAccessOrder(order);
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> retrieveOrderPayments(Long orderId) {
        Order order = findOrder(orderId);
        assertCanAccessOrder(order);
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    @Transactional
    public List<Order> addOrdersFromCart(List<CartItemRequest> cartItems) {
        if (!isClient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients can create orders");
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart must not be empty");
        }

        User currentUser = getCurrentUser();
        Map<Long, Integer> quantityByProductId = new LinkedHashMap<>();
        for (CartItemRequest item : cartItems) {
            validateCartItem(item);
            quantityByProductId.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Map<Long, List<PreparedItem>> itemsByShopId = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : quantityByProductId.entrySet()) {
            Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found: " + entry.getKey()));
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough stock for product " + product.getId());
            }
            Long shopId = product.getShop().getId();
            itemsByShopId.computeIfAbsent(shopId, ignored -> new ArrayList<>())
                    .add(new PreparedItem(product, quantity));
        }

        List<Order> createdOrders = new ArrayList<>();
        for (Map.Entry<Long, List<PreparedItem>> entry : itemsByShopId.entrySet()) {
            Shop shop = findShop(entry.getKey());
            Order order = new Order();
            order.setUser(currentUser);
            order.setShop(shop);
            order.setStatus(OrderStatus.PENDING);

            BigDecimal total = BigDecimal.ZERO;
            for (PreparedItem preparedItem : entry.getValue()) {
                BigDecimal lineTotal = preparedItem.product().getPrice()
                        .multiply(BigDecimal.valueOf(preparedItem.quantity()));
                total = total.add(lineTotal);
            }
            order.setTotalAmount(total);
            Order savedOrder = orderRepository.save(order);

            List<OrderItem> orderItems = new ArrayList<>();
            for (PreparedItem preparedItem : entry.getValue()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                orderItem.setProduct(preparedItem.product());
                orderItem.setQuantity(preparedItem.quantity());
                // Snapshot price for historical consistency.
                orderItem.setUnitPrice(preparedItem.product().getPrice());
                orderItems.add(orderItem);
            }
            orderItemRepository.saveAll(orderItems);
            createdOrders.add(savedOrder);
        }

        return createdOrders;
    }

    @Override
    @Transactional
    public void cancelOrderByClient(Long id) {
        if (!isClient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients can cancel orders from this endpoint");
        }

        Order order = findOrder(id);
        assertClientOwnsOrder(order);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PENDING orders can be cancelled by client");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        if (!isOwner()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only owners can update order status");
        }
        if (newStatus == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "New status is required");
        }

        Order order = findOrder(id);
        if (!order.getShop().getOwner().getId().equals(getCurrentUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage orders from your shop");
        }

        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Completed or cancelled orders cannot be modified");
        }

        boolean allowed =
                (currentStatus == OrderStatus.PENDING && newStatus == OrderStatus.CANCELLED)
                        || (currentStatus == OrderStatus.PAID && (newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED))
                        || (currentStatus == OrderStatus.PROCESSING && (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.CANCELLED));
        if (!allowed) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid status transition");
        }

        if (newStatus == OrderStatus.CANCELLED
                && (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.PROCESSING)) {
            markLatestSuccessfulPaymentAsRefunded(order);
            restoreStockForOrder(order);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    private void restoreStockForOrder(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private void markLatestSuccessfulPaymentAsRefunded(Order order) {
        Payment payment = paymentRepository.findTopByOrderIdAndStatusOrderByCreatedAtDesc(order.getId(), PaymentStatus.SUCCESS)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Cannot refund: no successful payment found"));
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
    }

    private void validateCartItem(CartItemRequest item) {
        if (item == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart contains an invalid item");
        }
        if (item.getProductId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product id is required in cart item");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }
    }

    private void assertCanAccessOrder(Order order) {
        if (isAdmin()) {
            return;
        }
        if (isClient()) {
            assertClientOwnsOrder(order);
            return;
        }
        if (isOwner()) {
            assertOwnerOfShopOrAdmin(order.getShop());
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to access this order");
    }

    private void assertClientOwnsOrder(Order order) {
        if (!order.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only access your own orders");
        }
    }

    private void assertOwnerOfShopOrAdmin(Shop shop) {
        if (isAdmin()) {
            return;
        }
        if (!isOwner() || !shop.getOwner().getId().equals(getCurrentUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage orders from your shop");
        }
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private Shop findShop(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
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

    private record PreparedItem(Product product, int quantity) {
    }
}
