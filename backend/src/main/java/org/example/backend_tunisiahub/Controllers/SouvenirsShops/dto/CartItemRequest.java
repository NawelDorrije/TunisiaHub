package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {
    private Long productId;
    private Integer quantity;
}
