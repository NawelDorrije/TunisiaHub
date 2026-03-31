package org.example.backend_tunisiahub.Services.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Product;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.ReviewType;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ProductRepository;
import org.example.backend_tunisiahub.Repositories.SouvenirsShops.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

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
        return reviewRepository.findByReviewTypeAndTargetId(ReviewType.PRODUCT, productId);
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public Product modifyProduct(Product product) {
        return productRepository.save(product);
    }
}
