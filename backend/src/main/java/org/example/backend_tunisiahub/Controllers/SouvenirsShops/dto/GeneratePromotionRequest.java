package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneratePromotionRequest {
    private Long shopId;
    private Long productId;
    private String language;
    private String platform;
    private String tone;
    private String colorTheme;
}
