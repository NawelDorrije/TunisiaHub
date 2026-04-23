package org.example.backend_tunisiahub.Services.SouvenirsShops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AiOrderMessageService aiOrderMessageService;
    @Mock
    private OrderIssueDetectorService orderIssueDetectorService;

    @InjectMocks
    private OrderService orderService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanAdvanceOrderStatusWithoutOwningShop() {
        User admin = user(1L, "admin@tunisiahub.test", RoleUser.ADMIN);
        User owner = user(2L, "owner@tunisiahub.test", RoleUser.OWNER);
        Order order = order(owner, OrderStatus.PENDING);

        authenticate(admin.getEmail());
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(admin);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.updateOrderStatus(1L, OrderStatus.PAID, false);

        assertEquals(OrderStatus.PAID, updated.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void adminCannotMoveOrderStatusBackward() {
        User admin = user(1L, "admin@tunisiahub.test", RoleUser.ADMIN);
        User owner = user(2L, "owner@tunisiahub.test", RoleUser.OWNER);
        Order order = order(owner, OrderStatus.DELIVERED);

        authenticate(admin.getEmail());
        when(userRepository.findByEmail(admin.getEmail())).thenReturn(admin);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> orderService.updateOrderStatus(1L, OrderStatus.PENDING, false)
        );

        assertEquals("Invalid status transition", exception.getMessage());
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null)
        );
    }

    private User user(Long id, String email, RoleUser role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    private Order order(User owner, OrderStatus status) {
        Shop shop = new Shop();
        shop.setId(10L);
        shop.setOwner(owner);

        Order order = new Order();
        order.setId(1L);
        order.setShop(shop);
        order.setStatus(status);
        order.setTotalAmount(BigDecimal.TEN);
        return order;
    }
}
