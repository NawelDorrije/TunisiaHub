package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import org.example.backend_tunisiahub.Entities.SouvenirsShops.OrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrderStatusRequest {
    private OrderStatus status;
    private Boolean generateAiMessage;
}
