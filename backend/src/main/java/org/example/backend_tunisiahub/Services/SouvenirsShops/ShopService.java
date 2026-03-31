package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.OrderRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ShopRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopService implements IShopService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

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
        return orderRepository.findByShopId(shopId);
    }

    @Override
    public List<Review> retrieveReviewsByShop(Long shopId) {
        return reviewRepository.findByReviewTypeAndTargetId(ReviewType.SHOP, shopId);
    }

    @Override
    public Shop addShop(Shop shop) {
        return shopRepository.save(shop);
    }

    @Override
    public void deleteShop(Long id) {
        shopRepository.deleteById(id);
    }

    @Override
    public Shop modifyShop(Shop shop) {
        return shopRepository.save(shop);
    }
}
