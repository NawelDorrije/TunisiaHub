package org.example.backend_tunisiahub.Controllers;

import jakarta.validation.Valid;
import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.QRScanResultDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Services.IPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for all payment operations.
 *
 * <pre>
 * POST /api/payments/deposit/{reservationId}   – pay deposit & receive QR code
 * POST /api/payments/{id}/refund               – refund deposit
 * POST /api/payments/{id}/settle               – settle remaining at reception
 * GET  /api/payments/scan                      – scan QR code (reception)
 * GET  /api/payments/{id}                      – get payment by id
 * GET  /api/payments/reservation/{id}          – get payment by reservation
 * GET  /api/payments                           – list all payments
 * POST /api/payments/{id}/resend               – resend confirmation email
 * </pre>
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService paymentService;

    public PaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── Client endpoints ──────────────────────────────────────────────────────

    /**
     * Pay the deposit to confirm a reservation.
     *
     * @param reservationId    which reservation to pay for
     * @param method           online payment method (CREDIT_CARD, PAYPAL, BANK_TRANSFER)
     * @param depositPercent   percentage to pay now; must be >= server minimum (default 30%).
     *                         Omit to use the configured minimum.
     * @param remainingMethod  how the remaining balance will be settled (CASH / CARD_AT_RECEPTION).
     *                         Required when depositPercent < 100.
     * @param clientEmail      email address for the confirmation + QR code
     */
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

    /**
     * Refund the deposit and cancel the reservation.
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentDTO> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    /**
     * Resend the confirmation email + QR code to the given address.
     */
    @PostMapping("/{id}/resend")
    public ResponseEntity<Void> resendConfirmation(
            @PathVariable Long id,
            @RequestParam String email) {
        paymentService.resendConfirmation(id, email);
        return ResponseEntity.noContent().build();
    }

    // ── Reception endpoints ───────────────────────────────────────────────────

    /**
     * Scan a client's check-in QR code at reception.
     * Automatically marks the reservation as ACTIVE if valid.
     *
     * @param token  the validation token encoded in the QR code
     */
    @GetMapping("/scan")
    public ResponseEntity<QRScanResultDTO> scanQRCode(@RequestParam String token) {
        return ResponseEntity.ok(paymentService.scanQRCode(token));
    }

    /**
     * Settle the remaining balance at reception (cash or card).
     *
     * @param id              payment id
     * @param receptionMethod CASH or CARD_AT_RECEPTION
     */
    @PostMapping("/{id}/settle")
    public ResponseEntity<PaymentDTO> settleRemaining(
            @PathVariable Long id,
            @RequestParam PaymentMethod receptionMethod) {
        return ResponseEntity.ok(paymentService.settleRemainingBalance(id, receptionMethod));
    }

    // ── Query endpoints ───────────────────────────────────────────────────────

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