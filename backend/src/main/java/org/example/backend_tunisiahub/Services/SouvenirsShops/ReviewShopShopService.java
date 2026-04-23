package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.OwnerReviewInsightsResponse;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.ReviewEligibilityResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
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
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewShopShopService implements IReviewShopService {

    private static final List<OrderStatus> DELIVERED_STATUSES = List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.DELIVERED, OrderStatus.COMPLETED);

    private final ReviewShopRepository reviewShopRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AiReviewInsightsService aiReviewInsightsService;

    @Override
    public Review retrieveReview(Long id) {
        return reviewShopRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
    }

    @Override
    public List<Review> retrieveReviewsByShop(Long shopId) {
        findShop(shopId);
        return reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(ReviewType.SHOP, shopId);
    }

    @Override
    public List<Review> retrieveReviewsByProduct(Long productId) {
        findProduct(productId);
        return reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(ReviewType.PRODUCT, productId);
    }

    @Override
    public List<Review> retrieveAllReviews() {
        return reviewShopRepository.findByDeletedFalseOrderByCreatedAtDesc();
    }

    @Override
    @Transactional
    public Review addShopReview(Long shopId, Integer rating, String comment) {
        validateRatingAndComment(rating, comment);
        User currentUser = getCurrentUser();
        assertClientCanWrite(currentUser);
        Shop shop = findShop(shopId);
        assertNotOwnerOfTargetShop(currentUser, shop);

        // 1) Must have at least one delivered/completed order in this shop.
        boolean hasDeliveredOrder = orderRepository.existsByUserIdAndShopIdAndStatusIn(
                currentUser.getId(), shopId, DELIVERED_STATUSES);
        if (!hasDeliveredOrder) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only review a shop after a delivered order");
        }

        // 2) Must not have an active review already.
        if (reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                currentUser.getId(), ReviewType.SHOP, shopId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have an active review for this shop");
        }

        Review review = reviewShopRepository.findByUserIdAndReviewTypeAndTargetId(
                        currentUser.getId(), ReviewType.SHOP, shopId)
                .orElseGet(Review::new);
        review.setUser(currentUser);
        review.setReviewType(ReviewType.SHOP);
        review.setTargetId(shopId);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setDeleted(false);
        review.setDeletedAt(null);

        Review saved = reviewShopRepository.save(review);
        double average = recalculateShopAverage(shop);
        saved.setAverageRating(average);
        return reviewShopRepository.save(saved);
    }

    @Override
    @Transactional
    public Review addProductReview(Long productId, Integer rating, String comment) {
        validateRatingAndComment(rating, comment);
        User currentUser = getCurrentUser();
        assertClientCanWrite(currentUser);
        Product product = findProduct(productId);
        assertNotOwnerOfTargetProduct(currentUser, product);

        // 1) Must have at least one delivered/completed order item for this product.
        boolean hasDeliveredOrder = orderItemRepository.existsByUserAndProductAndOrderStatusIn(
                currentUser.getId(), productId, DELIVERED_STATUSES);
        if (!hasDeliveredOrder) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only review a product after a delivered order");
        }

        // 2) Must not have an active review already.
        if (reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                currentUser.getId(), ReviewType.PRODUCT, productId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have an active review for this product");
        }

        Review review = reviewShopRepository.findByUserIdAndReviewTypeAndTargetId(
                        currentUser.getId(), ReviewType.PRODUCT, productId)
                .orElseGet(Review::new);
        review.setUser(currentUser);
        review.setReviewType(ReviewType.PRODUCT);
        review.setTargetId(productId);
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setDeleted(false);
        review.setDeletedAt(null);

        Review saved = reviewShopRepository.save(review);
        double average = recalculateProductAverage(product);
        saved.setAverageRating(average);
        return reviewShopRepository.save(saved);
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = retrieveReview(id);
        if (!isAdminFromAuth()) {
            User currentUser = getCurrentUser();
            assertClientCanWrite(currentUser);
            assertReviewOwner(review, currentUser);
        }
        if (Boolean.TRUE.equals(review.getDeleted())) {
            return;
        }

        review.setDeleted(true);
        review.setDeletedAt(LocalDateTime.now());
        reviewShopRepository.save(review);

        if (review.getReviewType() == ReviewType.SHOP) {
            recalculateShopAverage(findShop(review.getTargetId()));
        } else {
            recalculateProductAverage(findProduct(review.getTargetId()));
        }
    }

    @Override
    @Transactional
    public Review modifyReview(Long reviewId, Integer rating, String comment) {
        validateRatingAndComment(rating, comment);
        Review review = retrieveReview(reviewId);
        User currentUser = getCurrentUser();
        assertClientCanWrite(currentUser);
        assertReviewOwner(review, currentUser);
        if (Boolean.TRUE.equals(review.getDeleted())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Deleted review cannot be edited");
        }

        review.setRating(rating);
        review.setComment(comment.trim());
        Review saved = reviewShopRepository.save(review);

        double average;
        if (saved.getReviewType() == ReviewType.SHOP) {
            average = recalculateShopAverage(findShop(saved.getTargetId()));
        } else {
            average = recalculateProductAverage(findProduct(saved.getTargetId()));
        }
        saved.setAverageRating(average);
        return reviewShopRepository.save(saved);
    }

    private double recalculateShopAverage(Shop shop) {
        List<Review> reviews = reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(
                ReviewType.SHOP, shop.getId());
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        shop.setAverageRating(average);
        shopRepository.save(shop);
        return average;
    }

    private double recalculateProductAverage(Product product) {
        List<Review> reviews = reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(
                ReviewType.PRODUCT, product.getId());
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        product.setAverageRating(average);
        productRepository.save(product);
        return average;
    }

    private void validateRatingAndComment(Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comment is required");
        }
    }

    private Shop findShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }
        return user;
    }

    private void assertReviewOwner(Review review, User currentUser) {
        if (review.getUser() == null || !review.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only modify your own review");
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        return authentication.getName();
    }

    private void assertNotOwnerOfTargetShop(User currentUser, Shop shop) {
        if (shop.getOwner() != null && shop.getOwner().getId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Shop owners cannot review their own shop");
        }
    }

    private void assertNotOwnerOfTargetProduct(User currentUser, Product product) {
        if (product.getShop() != null
                && product.getShop().getOwner() != null
                && product.getShop().getOwner().getId().equals(currentUser.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Shop owners cannot review products from their own shop");
        }
    }

    private void assertClientCanWrite(User currentUser) {
        if (currentUser.getRole() != RoleUser.CLIENT && currentUser.getRole() != RoleUser.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only clients and admins can write, edit, or delete reviews");
        }
    }

    @Override
    public ReviewEligibilityResponse getReviewsWithEligibilityForShop(Long shopId) {
        Shop shop = findShop(shopId);
        List<Review> reviews = retrieveReviewsByShop(shopId);

        User currentUser = null;
        boolean canWriteReview = false;
        Review userReview = null;

        try {
            currentUser = getCurrentUser();
            boolean isClient = isClientUser();

            if (isClient) {
                // Check if client has delivered order and no existing review
                boolean hasDeliveredOrder = orderRepository.existsByUserIdAndShopIdAndStatusIn(
                        currentUser.getId(), shopId, DELIVERED_STATUSES);
                boolean hasExistingReview = reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                        currentUser.getId(), ReviewType.SHOP, shopId).isPresent();

                canWriteReview = hasDeliveredOrder && !hasExistingReview;
            }

            // Get user's review if exists
            userReview = reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                    currentUser.getId(), ReviewType.SHOP, shopId).orElse(null);

        } catch (ApiException e) {
            // If not authenticated or not found, canWriteReview = false, userReview = null
        }

        return new ReviewEligibilityResponse(reviews, canWriteReview, userReview);
    }

    @Override
    public ReviewEligibilityResponse getReviewsWithEligibilityForProduct(Long productId) {
        Product product = findProduct(productId);
        List<Review> reviews = retrieveReviewsByProduct(productId);

        User currentUser = null;
        boolean canWriteReview = false;
        Review userReview = null;

        try {
            currentUser = getCurrentUser();
            boolean isClient = isClientUser();

            if (isClient) {
                // Check if client has delivered order for this product and no existing review
                boolean hasDeliveredOrder = orderItemRepository.existsByUserAndProductAndOrderStatusIn(
                        currentUser.getId(), productId, DELIVERED_STATUSES);
                boolean hasExistingReview = reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                        currentUser.getId(), ReviewType.PRODUCT, productId).isPresent();

                canWriteReview = hasDeliveredOrder && !hasExistingReview;
            }

            // Get user's review if exists
            userReview = reviewShopRepository.findByUserIdAndReviewTypeAndTargetIdAndDeletedFalse(
                    currentUser.getId(), ReviewType.PRODUCT, productId).orElse(null);

        } catch (ApiException e) {
            // If not authenticated or not found, canWriteReview = false, userReview = null
        }

        return new ReviewEligibilityResponse(reviews, canWriteReview, userReview);
    }

    @Override
    public OwnerReviewInsightsResponse getOwnerReviewInsights() {
        return getOwnerReviewInsights(null, null);
    }

    @Override
    public OwnerReviewInsightsResponse getOwnerReviewInsights(Long shopId, String productIds) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != RoleUser.OWNER && currentUser.getRole() != RoleUser.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only owners and admins can analyze owner reviews");
        }

        List<Shop> shops;
        List<Product> products;

        if (currentUser.getRole() == RoleUser.ADMIN) {
            shops = shopRepository.findAll();
            products = productRepository.findAll();
        } else {
            Long ownerId = currentUser.getId();
            shops = shopRepository.findByOwnerId(ownerId);
            products = productRepository.findByShopOwnerId(ownerId);
        }

        // Filter by shop if specified
        if (shopId != null) {
            shops = shops.stream()
                    .filter(shop -> shop.getId().equals(shopId))
                    .toList();
            products = products.stream()
                    .filter(product -> product.getShop() != null && product.getShop().getId().equals(shopId))
                    .toList();
        }

        // Filter by products if specified
        if (productIds != null && !productIds.isEmpty()) {
            List<Long> productIdList = Arrays.stream(productIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();
            products = products.stream()
                    .filter(product -> productIdList.contains(product.getId()))
                    .toList();
        }

        List<Review> shopReviews = findActiveReviewsForTargets(
                ReviewType.SHOP,
                shops.stream().map(Shop::getId).toList()
        );
        List<Review> productReviews = findActiveReviewsForTargets(
                ReviewType.PRODUCT,
                products.stream().map(Product::getId).toList()
        );

        return aiReviewInsightsService.generateOwnerInsights(
                currentUser,
                shops,
                products,
                shopReviews,
                productReviews
        );
    }

    private boolean isClientUser() {
        RoleUser role = getCurrentUser().getRole();
        return role == RoleUser.CLIENT || role == RoleUser.ADMIN;
    }

    private boolean isAdmin(User currentUser) {
        return currentUser.getRole() == RoleUser.ADMIN;
    }

    private boolean isAdminFromAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private List<Review> findActiveReviewsForTargets(ReviewType reviewType, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return new ArrayList<>();
        }
        return reviewShopRepository.findByReviewTypeAndTargetIdInAndDeletedFalseOrderByCreatedAtDesc(reviewType, targetIds);
    }
}
