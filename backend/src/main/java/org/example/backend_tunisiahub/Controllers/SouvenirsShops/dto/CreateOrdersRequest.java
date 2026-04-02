package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrdersRequest {
    private List<CartItemRequest> items;
}
