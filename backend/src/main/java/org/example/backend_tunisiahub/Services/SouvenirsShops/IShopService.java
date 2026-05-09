package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.NearbyShopResponse;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;

public interface IShopService {

    List<Shop> retrieveAllShops();

    Shop retrieveShop(Long id);

    List<Shop> retrieveShopsByOwner(Long ownerId);

    List<Product> retrieveProductsByShop(Long shopId);

    List<Order> retrieveOrdersByShop(Long shopId);

    List<Review> retrieveReviewsByShop(Long shopId);

    List<NearbyShopResponse> findNearestShops(double latitude, double longitude, Double radiusKm, Integer limit);

    Shop addShop(Shop shop);

    void deleteShop(Long id);

    Shop modifyShop(Shop shop);
}
