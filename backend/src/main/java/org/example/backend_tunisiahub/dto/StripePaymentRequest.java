package org.example.backend_tunisiahub.dto;

//import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@AllArgsConstructor

public class StripePaymentRequest {
        private Long reservationId;
        private double amount;


}