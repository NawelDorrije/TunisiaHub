package org.example.backend_tunisiahub.Controllers;


import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Services.IPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/pay/{reservationId}")
    public ResponseEntity<PaymentDTO> pay(
            @PathVariable Long reservationId,
            @RequestParam PaymentMethod method) {
        return ResponseEntity.ok(paymentService.processPayment(reservationId, method));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentDTO> getByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getByReservationId(reservationId));
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDTO> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }
}