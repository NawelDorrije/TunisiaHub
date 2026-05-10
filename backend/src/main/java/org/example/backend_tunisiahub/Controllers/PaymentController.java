package org.example.backend_tunisiahub.Controllers;

import com.stripe.exception.StripeException;
import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.QRScanResultDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Services.IPaymentService;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentIntentRequest;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentIntentResponse;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentStatusResponse;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.StripeConfirmResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pour toutes les opérations de paiement.
 *
 * Endpoints Stripe (nouveaux) :
 *   POST /api/payments/stripe/create-payment-intent
 *   GET  /api/payments/stripe/status/{paymentIntentId}
 *   POST /api/payments/stripe/confirm/{paymentIntentId}
 *
 * Endpoints existants (inchangés) :
 *   POST /api/payments/deposit/{reservationId}
 *   POST /api/payments/{id}/refund
 *   POST /api/payments/{id}/settle
 *   GET  /api/payments/scan
 *   GET  /api/payments/{id}
 *   GET  /api/payments/reservation/{id}
 *   GET  /api/payments
 *   POST /api/payments/{id}/resend
 */
@RestController("campingPaymentController")
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STRIPE — nouveaux endpoints
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Étape 1 : crée un PaymentIntent et retourne le clientSecret au frontend.
     * La clé secrète Stripe ne quitte JAMAIS le backend.
     */
    @PostMapping("/stripe/create-payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @RequestBody PaymentIntentRequest request) {
        try {
            return ResponseEntity.ok(paymentService.createStripePaymentIntent(request));
        } catch (StripeException e) {
            throw new RuntimeException("Erreur Stripe : " + e.getMessage(), e);
        }
    }



    /**
     * Étape 2 : vérifie le statut d'un PaymentIntent (polling après paiement).
     */
    @GetMapping("/stripe/status/{paymentIntentId}")
    public ResponseEntity<PaymentStatusResponse> getStripeStatus(
            @PathVariable String paymentIntentId) {
        try {
            return ResponseEntity.ok(paymentService.getStripePaymentStatus(paymentIntentId));
        } catch (StripeException e) {
            throw new RuntimeException("Erreur Stripe : " + e.getMessage(), e);
        }
    }

    /**
     * Étape 3 : appelé par le frontend après confirmation Stripe.js.
     * Vérifie le succès avec Stripe puis finalise la réservation (QR, email, statut).
     */
    @PostMapping("/stripe/confirm/{paymentIntentId}")
    public ResponseEntity<StripeConfirmResponse> confirmStripePayment(
      @PathVariable String paymentIntentId,
      @RequestBody PaymentIntentRequest request) {

      System.out.println("PAYMENT INTENT ID = " + paymentIntentId);
      System.out.println("REQUEST = " + request);

      try {
        return ResponseEntity.ok(
          paymentService.confirmStripeAndFinalize(paymentIntentId, request));
      } catch (Exception e) {
        e.printStackTrace(); // 🔥 IMPORTANT
        throw new RuntimeException("Stripe confirm failed: " + e.getMessage(), e);
      }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ENDPOINTS EXISTANTS — inchangés
    // ══════════════════════════════════════════════════════════════════════════

    @PostMapping("/deposit/{reservationId}")
    public ResponseEntity<PaymentDTO> payDeposit(
            @PathVariable Long reservationId,
            @RequestParam PaymentMethod method,
            @RequestParam(required = false) Integer depositPercent,
            @RequestParam(required = false) PaymentMethod remainingMethod,
            @RequestParam String clientEmail) {
        return ResponseEntity.ok(
                paymentService.processDeposit(
                        reservationId, method, depositPercent, remainingMethod, clientEmail));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDTO> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<Void> resendConfirmation(
            @PathVariable Long id, @RequestParam String email) {
        paymentService.resendConfirmation(id, email);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/scan")
    public ResponseEntity<QRScanResultDTO> scanQRCode(@RequestParam String token) {
        return ResponseEntity.ok(paymentService.scanQRCode(token));
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<PaymentDTO> settleRemaining(
            @PathVariable Long id, @RequestParam PaymentMethod receptionMethod) {
        return ResponseEntity.ok(paymentService.settleRemainingBalance(id, receptionMethod));
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
}
