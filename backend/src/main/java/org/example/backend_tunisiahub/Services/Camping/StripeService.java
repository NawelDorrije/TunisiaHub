package org.example.backend_tunisiahub.Services.Camping;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Services.IPaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class StripeService {

    @Value("${stripe.secret.key}")   // ← secret key : JAMAIS exposée au frontend
    private String stripeSecretKey;

    private final IPaymentService paymentService;

    public StripeService(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialisé");
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    public record PaymentIntentRequest(
            Long       reservationId,
            Integer    depositPercent,    // null = défaut backend (30%)
            String     method,            // "CREDIT_CARD" | "PAYPAL" | "BANK_TRANSFER"
            String     remainingMethod,   // "CASH" | "CARD_AT_RECEPTION"
            String     clientEmail,
            BigDecimal totalAmount        // montant total affiché côté client (validation seulement)
    ) {}

    public record PaymentIntentResponse(
            String     clientSecret,      // envoyé au frontend pour Stripe.js
            String     paymentIntentId,   // pi_xxx
            BigDecimal depositAmount,
            String     currency
    ) {}

    public record PaymentStatusResponse(
            String  paymentIntentId,
            String  status,
            boolean succeeded
    ) {}

    public record ConfirmPaymentResponse(
            String     paymentIntentId,
            String     stripeStatus,
            PaymentDTO reservation
    ) {}

    // ── Créer le PaymentIntent ────────────────────────────────────────────────

    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest req)
            throws StripeException {

        if (req.totalAmount() == null || req.totalAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Montant invalide");

        int pct = req.depositPercent() != null ? req.depositPercent() : 30;

        BigDecimal deposit = req.totalAmount()
                .multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // TND = devise à 3 décimales → Stripe reçoit millimes (× 1000)
        long stripeAmount = deposit
                .multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(stripeAmount)
                .setCurrency("tnd")
                .setDescription("TunisiaHub Camping – Réservation #" + req.reservationId())
                .putMetadata("reservationId",  String.valueOf(req.reservationId()))
                .putMetadata("depositPercent", String.valueOf(pct))
                .putMetadata("clientEmail",    req.clientEmail())
                .putMetadata("remainingMethod",
                        req.remainingMethod() != null ? req.remainingMethod() : "NONE")
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("PaymentIntent créé : {} – réservation {} – montant {} TND",
                intent.getId(), req.reservationId(), deposit);

        return new PaymentIntentResponse(
                intent.getClientSecret(), intent.getId(), deposit, "tnd");
    }

    // ── Vérifier le statut ────────────────────────────────────────────────────

    public PaymentStatusResponse getPaymentStatus(String paymentIntentId)
            throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        return new PaymentStatusResponse(
                intent.getId(), intent.getStatus(),
                "succeeded".equals(intent.getStatus()));
    }

    // ── Confirmer et finaliser ────────────────────────────────────────────────

    public ConfirmPaymentResponse confirmAndFinalizePayment(
            String paymentIntentId, PaymentIntentRequest req) throws StripeException {

        // 1. Vérifier avec Stripe que le paiement a bien réussi
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus()))
            throw new RuntimeException("Paiement non confirmé. Statut : " + intent.getStatus());

        // 2. Résoudre la méthode de paiement
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        try { method = PaymentMethod.valueOf(req.method()); }
        catch (IllegalArgumentException ignored) {}

        PaymentMethod remainingMethod = null;
        if (req.remainingMethod() != null && !"NONE".equals(req.remainingMethod())) {
            try { remainingMethod = PaymentMethod.valueOf(req.remainingMethod()); }
            catch (IllegalArgumentException ignored) {}
        }

        // 3. Déléguer au service existant (QR, email, statut réservation)
        PaymentDTO dto = paymentService.processDeposit(
                req.reservationId(), method,
                req.depositPercent(), remainingMethod, req.clientEmail());

        log.info("Paiement Stripe {} confirmé – réservation {} finalisée",
                paymentIntentId, req.reservationId());

        return new ConfirmPaymentResponse(paymentIntentId, intent.getStatus(), dto);
    }
}