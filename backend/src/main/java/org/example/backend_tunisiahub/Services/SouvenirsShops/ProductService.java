package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewShopRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ReviewShopRepository reviewShopRepository;
    private final ShopRepository shopRepository;

    @Override
    public List<Product> retrieveAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product retrieveProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public List<Product> retrieveProductsByShop(Long shopId) {
        return productRepository.findByShopId(shopId);
    }

    @Override
    public List<Review> retrieveReviewsByProduct(Long productId) {
        return reviewShopRepository.findByReviewTypeAndTargetIdAndDeletedFalseOrderByCreatedAtDesc(ReviewType.PRODUCT, productId);
    }

    @Override
    public Product addProduct(Product product) {
        Shop shop = resolveAndValidateShop(product.getShop() == null ? null : product.getShop().getId());
        assertOwnerOrAdmin(shop.getOwner().getEmail());
        product.setShop(shop);
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        assertOwnerOrAdmin(existing.getShop().getOwner().getEmail());
        productRepository.deleteById(id);
    }

    @Override
    public Product modifyProduct(Product product) {
        if (product.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product id is required");
        }

        Product existing = productRepository.findById(product.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));

        Long shopId = existing.getShop() != null ? existing.getShop().getId() : null;
        if (product.getShop() != null && product.getShop().getId() != null) {
            shopId = product.getShop().getId();
        }
        Shop targetShop = resolveAndValidateShop(shopId);
        assertOwnerOrAdmin(targetShop.getOwner().getEmail());

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setPhotoUrl(product.getPhotoUrl());
        existing.setShop(targetShop);

        return productRepository.save(existing);
    }

    private Shop resolveAndValidateShop(Long shopId) {
        if (shopId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product must reference a shop id");
        }
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shop not found"));
    }

    private void assertOwnerOrAdmin(String ownerEmail) {
        if (isCurrentUserAdmin()) {
            return;
        }
        String currentUserEmail = getCurrentUserEmail();
        if (!currentUserEmail.equalsIgnoreCase(ownerEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not allowed to manage this product");
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
