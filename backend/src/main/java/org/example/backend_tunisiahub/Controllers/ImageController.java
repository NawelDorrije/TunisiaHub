package org.example.backend_tunisiahub.Controllers;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.dto.ImageUploadAnalysisResponse;
import org.example.backend_tunisiahub.Services.CloudinaryService;
import org.example.backend_tunisiahub.Services.SouvenirsShops.AiImageDescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final CloudinaryService cloudinaryService;
    private final AiImageDescriptionService aiImageDescriptionService;

    @PostMapping("/shop")
    public ResponseEntity<String> uploadShopImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = cloudinaryService.uploadImage(file, "shops");
        return ResponseEntity.ok(url);
    }

    @PostMapping("/product")
    public ResponseEntity<String> uploadProductImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = cloudinaryService.uploadImage(file, "products");
        return ResponseEntity.ok(url);
    }

    @PostMapping("/shop/describe")
    public ResponseEntity<ImageUploadAnalysisResponse> uploadShopImageWithDescription(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "city", required = false) String city
    ) throws IOException {
        String url = cloudinaryService.uploadImage(file, "shops");
        String suggestedDescription = aiImageDescriptionService.generateShopDescription(file, name, category, city);
        return ResponseEntity.ok(new ImageUploadAnalysisResponse(url, suggestedDescription));
    }

    @PostMapping("/product/describe")
    public ResponseEntity<ImageUploadAnalysisResponse> uploadProductImageWithDescription(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "shopName", required = false) String shopName,
            @RequestParam(value = "price", required = false) java.math.BigDecimal price
    ) throws IOException {
        String url = cloudinaryService.uploadImage(file, "products");
        String suggestedDescription = aiImageDescriptionService.generateProductDescription(file, name, shopName, price);
        return ResponseEntity.ok(new ImageUploadAnalysisResponse(url, suggestedDescription));
    }
}
