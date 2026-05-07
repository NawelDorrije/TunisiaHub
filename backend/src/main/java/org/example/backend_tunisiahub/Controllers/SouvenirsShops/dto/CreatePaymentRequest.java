package org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.PaymentMethod;

@Getter
@Setter
public class CreatePaymentRequest {
    private Long orderId;
    private PaymentMethod method;
    private String transactionReference;
    private Boolean simulateFailure;
}
