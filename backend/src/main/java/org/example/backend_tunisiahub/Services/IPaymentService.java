package org.example.backend_tunisiahub.Services;

import com.stripe.exception.StripeException;
import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.QRScanResultDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentIntentRequest;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentIntentResponse;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.PaymentStatusResponse;
import org.example.backend_tunisiahub.Services.PaymentServiceImpl.StripeConfirmResponse;

import java.util.List;

public interface IPaymentService {

    // ── Stripe ────────────────────────────────────────────────────────────────

    /** Étape 1 : crée un PaymentIntent Stripe et retourne le clientSecret */
    PaymentIntentResponse createStripePaymentIntent(PaymentIntentRequest request)
            throws StripeException;

    /** Étape 2 : vérifie le statut d'un PaymentIntent (polling frontend) */
    PaymentStatusResponse getStripePaymentStatus(String paymentIntentId)
            throws StripeException;

    /** Étape 3 : vérifie le succès Stripe, puis finalise la réservation */
    StripeConfirmResponse confirmStripeAndFinalize(String paymentIntentId,
                                                   PaymentIntentRequest request)
            throws StripeException;

    // ── Paiement classique ────────────────────────────────────────────────────

    PaymentDTO processDeposit(Long reservationId,
                              PaymentMethod method,
                              Integer depositPercent,
                              PaymentMethod remainingMethod,
                              String clientEmail);

    PaymentDTO refund(Long paymentId);

    QRScanResultDTO scanQRCode(String qrToken);

    PaymentDTO settleRemainingBalance(Long paymentId, PaymentMethod receptionMethod);

    PaymentDTO getByReservationId(Long reservationId);

    PaymentDTO getById(Long id);

    List<PaymentDTO> getAll();

    void resendConfirmation(Long paymentId, String email);
}