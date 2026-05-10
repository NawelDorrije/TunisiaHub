package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.GeneratePromotionRequest;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.PromotionCaptionResponse;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.PromotionImageResponse;
import org.example.backend_tunisiahub.Services.SouvenirsShops.PromotionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/souvenir-shops/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping("/caption")
    public PromotionCaptionResponse generateCaption(@RequestBody GeneratePromotionRequest request) {
        return new PromotionCaptionResponse(promotionService.generateCaption(request));
    }

    @PostMapping("/image")
    public PromotionImageResponse generateImage(@RequestBody GeneratePromotionRequest request) {
        return new PromotionImageResponse(promotionService.generateImageUrl(request));
    }
}
