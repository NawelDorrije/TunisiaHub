package org.example.backend_tunisiahub.Services.SouvenirsShops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.OwnerReviewInsightsResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderItemRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewShopRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReviewShopShopServiceTest {

    @Mock
    private ReviewShopRepository reviewShopRepository;
    @Mock
    private ShopRepository shopRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AiReviewInsightsService aiReviewInsightsService;

    @InjectMocks
    private ReviewShopShopService reviewService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanSoftDeleteReviewAndRecalculateShopRating() {
        User admin = user(1L, "admin@tunisiahub.test", RoleUser.ADMIN);
        User client = user(2L, "client@tunisiahub.test", RoleUser.CLIENT);
        Review review = review(100L, client, 10L, 5);
        Review remainingReview = review(101L, client, 10L, 3);
        Shop shop = new Shop();
        shop.setId(10L);
        shop.setAverageRating(5.0);

        authenticate(admin.getEmail());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(reviewShopRepository.findById(100L)).thenReturn(Optional.of(review));
        when(reviewShopRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(ReviewType.SHOP, 10L))
                .thenReturn(List.of(remainingReview));

        reviewService.deleteReview(100L);

        assertEquals(Boolean.TRUE, review.getDeleted());
        assertNotNull(review.getDeletedAt());
        assertEquals(3.0, shop.getAverageRating());
        verify(reviewShopRepository).save(review);
        verify(shopRepository).save(shop);
    }

    @Test
    void ownerSummaryUsesOnlyCurrentOwnerData() {
        User owner = user(5L, "owner@tunisiahub.test", RoleUser.OWNER);
        Shop shop = new Shop();
        shop.setId(10L);
        shop.setName("Medina Crafts");
        shop.setOwner(owner);

        Product product = new Product();
        product.setId(20L);
        product.setName("Plate");
        product.setShop(shop);

        Review shopReview = review(100L, owner, 10L, 4);
        Review productReview = new Review();
        productReview.setId(101L);
        productReview.setUser(owner);
        productReview.setReviewType(ReviewType.PRODUCT);
        productReview.setTargetId(20L);
        productReview.setRating(5);
        productReview.setComment("Excellent quality");
        productReview.setDeleted(false);

        OwnerReviewInsightsResponse expected = new OwnerReviewInsightsResponse();
        expected.setOwnerId(owner.getId());

        authenticate(owner.getEmail());
        when(userRepository.findByEmail(owner.getEmail())).thenReturn(owner);
        when(shopRepository.findByOwnerId(owner.getId())).thenReturn(List.of(shop));
        when(productRepository.findByShopOwnerId(owner.getId())).thenReturn(List.of(product));
        when(reviewShopRepository.findByReviewTypeAndTargetIdInAndDeletedFalseOrderByCreatedAtDesc(ReviewType.SHOP, List.of(10L)))
                .thenReturn(List.of(shopReview));
        when(reviewShopRepository.findByReviewTypeAndTargetIdInAndDeletedFalseOrderByCreatedAtDesc(ReviewType.PRODUCT, List.of(20L)))
                .thenReturn(List.of(productReview));
        when(aiReviewInsightsService.generateOwnerInsights(eq(owner), eq(List.of(shop)), eq(List.of(product)), eq(List.of(shopReview)), eq(List.of(productReview))))
                .thenReturn(expected);

        OwnerReviewInsightsResponse actual = reviewService.getOwnerReviewInsights();

        assertSame(expected, actual);
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

    private Review review(Long id, User user, Long targetId, int rating) {
        Review review = new Review();
        review.setId(id);
        review.setUser(user);
        review.setReviewType(ReviewType.SHOP);
        review.setTargetId(targetId);
        review.setRating(rating);
        review.setComment("review");
        review.setDeleted(false);
        return review;
    }
}
