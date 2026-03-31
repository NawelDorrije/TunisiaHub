package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

public interface IProductService {

    List<Product> retrieveAllProducts();

    Product retrieveProduct(Long id);

    List<Product> retrieveProductsByShop(Long shopId);

    List<Review> retrieveReviewsByProduct(Long productId);

    Product addProduct(Product product);

    void deleteProduct(Long id);

    Product modifyProduct(Product product);
}
