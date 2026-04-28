package org.example.backend_tunisiahub.Controllers.Event;

import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.dto.StripePaymentRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stripe")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"})
@RequiredArgsConstructor
public class StripeController {

    @PostMapping("/create-payment-intent")
    public Map<String, Object> createPaymentIntent(@RequestBody StripePaymentRequest request) throws Exception {

        // ⚠ Stripe travaille en centimes
        long amount = (long) (request.getAmount() * 100);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency("usd") // ou "eur" (PAS TND !)
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods
                                        .builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .putMetadata("reservationId", String.valueOf(request.getReservationId()))
                        .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Map<String, Object> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());

        return response;
    }
}
