package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CreatePaymentRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderItem;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.PaymentMethod;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.PaymentStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
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
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    public List<Payment> retrieveAllPayments() {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can view all payments");
        }
        return paymentRepository.findAll();
    }

    @Override
    public Payment retrievePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
        assertCanAccessOrder(payment.getOrder());
        return payment;
    }

    @Override
    public List<Payment> retrievePaymentsByOrder(Long orderId) {
        Order order = findOrder(orderId);
        assertCanAccessOrder(order);
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    @Override
    @Transactional
    public Payment addPayment(CreatePaymentRequest request) {
        if (!isClient()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients can pay orders");
        }
        if (request == null || request.getOrderId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order id is required");
        }
        if (request.getMethod() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment method is required");
        }

        Order order = findOrder(request.getOrderId());
        assertClientOwnsOrder(order);
        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.PROCESSING
                || order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This order cannot be paid");
        }

        List<Payment> paymentHistory = paymentRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());
        if (!paymentHistory.isEmpty()) {
            Payment latest = paymentHistory.get(0);
            if (latest.getStatus() == PaymentStatus.SUCCESS) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Order payment already succeeded");
            }
            if (latest.getStatus() == PaymentStatus.PENDING) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "A payment attempt is already pending");
            }
            if (latest.getStatus() != PaymentStatus.FAILED) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot create new payment attempt");
            }
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(request.getMethod());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);

        if (request.getMethod() == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment = paymentRepository.save(payment);
            finalizeSuccessfulPayment(order);
            return payment;
        }

        boolean simulateFailure = Boolean.TRUE.equals(request.getSimulateFailure());
        if (simulateFailure) {
            payment.setStatus(PaymentStatus.FAILED);
            return paymentRepository.save(payment);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);
        finalizeSuccessfulPayment(order);
        return payment;
    }

    @Override
    public void deletePayment(Long id) {
        if (!isAdmin()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admin can delete payments");
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public Payment modifyPayment(Payment payment) {
        throw new ApiException(HttpStatus.BAD_REQUEST, "Use POST /payments to create a new payment attempt");
    }

    private void finalizeSuccessfulPayment(Order order) {
        validateAndDecrementStock(order);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
    }

    private void validateAndDecrementStock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock for product " + product.getId());
            }
        }
        for (OrderItem item : items) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
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
        throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to access this payment");
    }

    private void assertClientOwnsOrder(Order order) {
        if (!order.getUser().getId().equals(getCurrentUser().getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only pay your own orders");
        }
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
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
