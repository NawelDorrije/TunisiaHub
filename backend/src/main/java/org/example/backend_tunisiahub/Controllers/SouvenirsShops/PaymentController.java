package org.example.backend_tunisiahub.Controllers.SouvenirsShops;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.SouvenirsShops.dto.CreatePaymentRequest;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Payment;
import org.example.backend_tunisiahub.Services.SouvenirsShops.IPaymentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("souvenirShopPaymentController")
@RequestMapping("/api/souvenir-shops/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.retrieveAllPayments();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentService.retrievePayment(id);
    }

    @GetMapping("/order/{orderId}")
    public List<Payment> getPaymentsByOrder(@PathVariable Long orderId) {
        return paymentService.retrievePaymentsByOrder(orderId);
    }

    @PostMapping
    public Payment createPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.addPayment(request);
    }

    @PutMapping
    public Payment updatePayment(@RequestBody Payment payment) {
        return paymentService.modifyPayment(payment);
    }

    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }
}
