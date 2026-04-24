package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.NearbyShopResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
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

@Service
@RequiredArgsConstructor
public class ShopService implements IShopService {

    private static final int DEFAULT_NEARBY_LIMIT = 10;
    private static final int MAX_NEARBY_LIMIT = 50;
    private static final double EARTH_RADIUS_KM = 6371.0088d;

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewShopRepository reviewShopRepository;
    private final UserRepository userRepository;
    private final AiImageDescriptionService aiImageDescriptionService;

    @Override
    public List<Shop> retrieveAllShops() {
        return shopRepository.findAll();
    }

    @Override
    public Shop retrieveShop(Long id) {
        return shopRepository.findById(id).orElse(null);
    }

    @Override
    public List<Shop> retrieveShopsByOwner(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Product> retrieveProductsByShop(Long shopId) {
        return productRepository.findByShopId(shopId);
    }

    @Override
    public List<Order> retrieveOrdersByShop(Long shopId) {
        return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }

    @Override
    public List<Review> retrieveReviewsByShop(Long shopId) {
        return reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(ReviewType.SHOP, shopId);
    }

    @Override
    public List<NearbyShopResponse> findNearestShops(double latitude, double longitude, Integer limit) {
        validateCoordinates(latitude, longitude);

        int safeLimit = sanitizeLimit(limit);
        return shopRepository.findAll().stream()
                .filter(this::hasCoordinates)
                .map(shop -> toNearbyShopResponse(shop, latitude, longitude))
                .sorted((a, b) -> Double.compare(a.distanceKm(), b.distanceKm()))
                .limit(safeLimit)
                .toList();
    }

    @Override
    public Shop addShop(Shop shop) {
        User currentUser = getCurrentUser();
        shop.setOwner(currentUser);
        populateDescriptionIfMissing(shop);
        return shopRepository.save(shop);
    }

    @Override
    public void deleteShop(Long id) {
        Shop existing = shopRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
        assertOwnerOrAdmin(existing.getOwner().getEmail());
        shopRepository.deleteById(id);
    }

    @Override
    public Shop modifyShop(Shop shop) {
        if (shop.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shop id is required");
        }

        Shop existing = shopRepository.findById(shop.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
        assertOwnerOrAdmin(existing.getOwner().getEmail());

        existing.setName(shop.getName());
        existing.setDescription(shop.getDescription());
        existing.setCategory(shop.getCategory());
        existing.setCity(shop.getCity());
        existing.setAddress(shop.getAddress());
        existing.setLatitude(shop.getLatitude());
        existing.setLongitude(shop.getLongitude());
        existing.setPhotoUrl(shop.getPhotoUrl());
        populateDescriptionIfMissing(existing);

        return shopRepository.save(existing);
    }

    private void populateDescriptionIfMissing(Shop shop) {
        if (shop.getDescription() != null && !shop.getDescription().isBlank()) {
            return;
        }
        shop.setDescription(aiImageDescriptionService.generateShopDescriptionFromUrl(shop));
    }

    private User getCurrentUser() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found");
        }
        return user;
    }

    private void assertOwnerOrAdmin(String ownerEmail) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == RoleUser.ADMIN) {
            return;
        }
        if (!currentUser.getEmail().equalsIgnoreCase(ownerEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to manage this shop");
        }
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        return authentication.getName();
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "longitude must be between -180 and 180");
        }
    }

    private int sanitizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_NEARBY_LIMIT;
        }
        if (limit < 1 || limit > MAX_NEARBY_LIMIT) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "limit must be between 1 and " + MAX_NEARBY_LIMIT
            );
        }
        return limit;
    }

    private boolean hasCoordinates(Shop shop) {
        return shop.getLatitude() != null && shop.getLongitude() != null;
    }

    private NearbyShopResponse toNearbyShopResponse(Shop shop, double userLatitude, double userLongitude) {
        double distanceKm = haversineKm(
                userLatitude,
                userLongitude,
                shop.getLatitude(),
                shop.getLongitude()
        );

        return new NearbyShopResponse(
                shop.getId(),
                shop.getName(),
                shop.getDescription(),
                shop.getCategory(),
                shop.getCity(),
                shop.getAddress(),
                shop.getLatitude(),
                shop.getLongitude(),
                shop.getPhotoUrl(),
                shop.getAverageRating(),
                roundDistance(distanceKm)
        );
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private double roundDistance(double distanceKm) {
        return Math.round(distanceKm * 100.0d) / 100.0d;
    }
}
