package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Shop;
import org.example.backend_tunisiahub.Services.SouvenirsShops.IShopService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/souvenir-shops/shops")
@RequiredArgsConstructor
public class ShopController {

    private final IShopService shopService;

    @GetMapping
    public List<Shop> getAllShops() {
        return shopService.retrieveAllShops();
    }

    @GetMapping("/{id}")
    public Shop getShopById(@PathVariable Long id) {
        return shopService.retrieveShop(id);
    }

    @GetMapping("/owner/{ownerId}")
    public List<Shop> getShopsByOwner(@PathVariable Long ownerId) {
        return shopService.retrieveShopsByOwner(ownerId);
    }

    @GetMapping("/{id}/products")
    public List<Product> getProductsByShop(@PathVariable Long id) {
        return shopService.retrieveProductsByShop(id);
    }

    @GetMapping("/{id}/orders")
    public List<Order> getOrdersByShop(@PathVariable Long id) {
        return shopService.retrieveOrdersByShop(id);
    }

    @GetMapping("/{id}/reviews")
    public List<Review> getReviewsByShop(@PathVariable Long id) {
        return shopService.retrieveReviewsByShop(id);
    }

    @PostMapping
    public Shop createShop(@RequestBody Shop shop) {
        return shopService.addShop(shop);
    }

    @PutMapping
    public Shop updateShop(@RequestBody Shop shop) {
        return shopService.modifyShop(shop);
    }

    @DeleteMapping("/{id}")
    public void deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
    }
}
