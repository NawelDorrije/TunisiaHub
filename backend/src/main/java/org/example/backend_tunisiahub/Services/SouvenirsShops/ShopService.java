package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewShopRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService implements IShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewShopRepository reviewShopRepository;
    private final UserRepository userRepository;

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
        return reviewShopRepository.findByReviewTypeAndTargetId(ReviewType.SHOP, shopId);
    }

    @Override
    public Shop addShop(Shop shop) {
        User currentUser = getCurrentUser();
        shop.setOwner(currentUser);
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

        return shopRepository.save(existing);
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
        if (isCurrentUserAdmin()) {
            return;
        }
        String currentUserEmail = getCurrentUserEmail();
        if (!currentUserEmail.equalsIgnoreCase(ownerEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to manage this shop");
        }
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Missing authentication");
        }
        return authentication.getName();
    }
}
