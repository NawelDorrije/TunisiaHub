package org.example.backend_tunisiahub.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.QRScanResultDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.PaymentMapper;
import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.PaymentMethod;
import org.example.backend_tunisiahub.Entities.PaymentStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Repositories.PaymentRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Services.Camping.NotificationService;
import org.example.backend_tunisiahub.Services.Camping.QRCodeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PaymentServiceImpl implements IPaymentService {

    // ── Config ────────────────────────────────────────────────────────────────
    @Value("${app.payment.minimum-deposit-percent:30}")
    private int minimumDepositPercent;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;   // JAMAIS exposée au frontend

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialisé");
    }

    // ── DTOs Stripe (records imbriqués) ───────────────────────────────────────
    public record PaymentIntentRequest(
            Long       reservationId,
            Integer    depositPercent,   // null = défaut (30%)
            String     method,           // "CREDIT_CARD" | "PAYPAL" | "BANK_TRANSFER"
            String     remainingMethod,  // "CASH" | "CARD_AT_RECEPTION"
            String     clientEmail,
            BigDecimal totalAmount       // montant affiché côté client (validation seulement)
    ) {}

    public record PaymentIntentResponse(
            String     clientSecret,     // pour Stripe.js — pas la clé secrète
            String     paymentIntentId,  // pi_xxx
            BigDecimal depositAmount,
            String     currency
    ) {}

    public record PaymentStatusResponse(
            String  paymentIntentId,
            String  status,
            boolean succeeded
    ) {}

    public record StripeConfirmResponse(
            String     paymentIntentId,
            String     stripeStatus,
            PaymentDTO reservation
    ) {}

    // ── Méthodes autorisées ───────────────────────────────────────────────────
    private static final Set<PaymentMethod> ONLINE_METHODS = Set.of(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.PAYPAL,
            PaymentMethod.BANK_TRANSFER
    );
    private static final Set<PaymentMethod> RECEPTION_METHODS = Set.of(
            PaymentMethod.CASH,
            PaymentMethod.CARD_AT_RECEPTION
    );

    // ── Dépendances ───────────────────────────────────────────────────────────
    private final PaymentRepository     paymentRepository;
    private final ReservationRepository reservationRepository;
    private final IReservationService   reservationService;
    private final PaymentMapper         paymentMapper;
    private final QRCodeService         qrCodeService;
    private final NotificationService   notificationService;
    private final ObjectMapper          objectMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ReservationRepository reservationRepository,
                              IReservationService reservationService,
                              PaymentMapper paymentMapper,
                              QRCodeService qrCodeService,
                              NotificationService notificationService,
                              ObjectMapper objectMapper) {
        this.paymentRepository     = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService    = reservationService;
        this.paymentMapper         = paymentMapper;
        this.qrCodeService         = qrCodeService;
        this.notificationService   = notificationService;
        this.objectMapper          = objectMapper;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STRIPE — Étape 1 : créer le PaymentIntent
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentIntentResponse createStripePaymentIntent(PaymentIntentRequest req)
            throws StripeException {

        if (req.totalAmount() == null || req.totalAmount().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Montant invalide");

        int pct = req.depositPercent() != null ? req.depositPercent() : minimumDepositPercent;

        // Montant de l'acompte en TND (affiché côté frontend)
        BigDecimal deposit = req.totalAmount()
                .multiply(BigDecimal.valueOf(pct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Conversion TND → EUR pour Stripe (1 TND ≈ 0.30 EUR)
        BigDecimal RATE_TND_TO_EUR = new BigDecimal("0.30");
        BigDecimal depositInEur = deposit
                .multiply(RATE_TND_TO_EUR)
                .setScale(2, RoundingMode.HALF_UP);

        // EUR = 2 décimales → Stripe attend des centimes (× 100)
        long stripeAmount = depositInEur
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        // Stripe exige un minimum de 50 centimes (0.50 EUR)
        if (stripeAmount < 50) stripeAmount = 50;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(stripeAmount)
                .setCurrency("eur")
                .setDescription("TunisiaHub Camping – Réservation #" + req.reservationId())
                .putMetadata("reservationId",   String.valueOf(req.reservationId()))
                .putMetadata("depositPercent",  String.valueOf(pct))
                .putMetadata("clientEmail",     req.clientEmail())
                .putMetadata("remainingMethod",
                        req.remainingMethod() != null ? req.remainingMethod() : "NONE")
                .putMetadata("amountTND",       deposit.toPlainString())
                .putMetadata("amountEUR",       depositInEur.toPlainString())
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("PaymentIntent créé : {} – réservation {} – dépôt {} TND ({} EUR)",
                intent.getId(), req.reservationId(), deposit, depositInEur);

        return new PaymentIntentResponse(
                intent.getClientSecret(),
                intent.getId(),
                deposit,   // montant TND affiché côté frontend
                "eur"      // devise Stripe réelle
        );
    }
    // ══════════════════════════════════════════════════════════════════════════
    // STRIPE — Étape 2 : vérifier le statut
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentStatusResponse getStripePaymentStatus(String paymentIntentId)
            throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        return new PaymentStatusResponse(
                intent.getId(),
                intent.getStatus(),
                "succeeded".equals(intent.getStatus()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STRIPE — Étape 3 : confirmer côté backend et finaliser la réservation
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public StripeConfirmResponse confirmStripeAndFinalize(
            String paymentIntentId, PaymentIntentRequest req) throws StripeException {

        // 1. Vérifier avec Stripe que le paiement a bien réussi
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(intent.getStatus()))
            throw new RuntimeException("Paiement non confirmé par Stripe. Statut : " + intent.getStatus());

        // 2. Résoudre les méthodes de paiement
        PaymentMethod method = PaymentMethod.CREDIT_CARD;
        try { method = PaymentMethod.valueOf(req.method()); }
        catch (IllegalArgumentException ignored) {}

        PaymentMethod remainingMethod = null;
        if (req.remainingMethod() != null && !"NONE".equals(req.remainingMethod())) {
            try { remainingMethod = PaymentMethod.valueOf(req.remainingMethod()); }
            catch (IllegalArgumentException ignored) {}
        }

        // 3. Déléguer au processDeposit existant (QR, email, statut réservation)
        PaymentDTO dto = processDeposit(
                req.reservationId(), method,
                req.depositPercent(), remainingMethod, req.clientEmail());

        log.info("Paiement Stripe {} confirmé – réservation {} finalisée",
                paymentIntentId, req.reservationId());

        return new StripeConfirmResponse(paymentIntentId, intent.getStatus(), dto);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PAIEMENT EXISTANT — processDeposit (inchangé)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentDTO processDeposit(Long reservationId,
                                     PaymentMethod method,
                                     Integer depositPercent,
                                     PaymentMethod remainingMethod,
                                     String clientEmail) {

        if (!ONLINE_METHODS.contains(method))
            throw new RuntimeException("Deposit must be paid with an online method: " + ONLINE_METHODS);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING)
            throw new RuntimeException("Only PENDING reservations can be paid. Current status: "
                    + reservation.getStatus());

        if (paymentRepository.findByReservationId(reservationId).isPresent())
            throw new RuntimeException("A payment already exists for reservation " + reservationId);

        int effectivePercent = (depositPercent != null) ? depositPercent : minimumDepositPercent;
        if (effectivePercent < minimumDepositPercent)
            throw new RuntimeException(String.format(
                    "Minimum deposit is %d%%. You requested %d%%.",
                    minimumDepositPercent, effectivePercent));
        if (effectivePercent > 100)
            throw new RuntimeException("Deposit percent cannot exceed 100%.");

        boolean fullPayment = (effectivePercent == 100);
        if (!fullPayment) {
            if (remainingMethod == null)
                throw new RuntimeException(
                        "remainingPaymentMethod is required when deposit < 100%");
            if (!RECEPTION_METHODS.contains(remainingMethod))
                throw new RuntimeException(
                        "Remaining balance must be settled at reception: " + RECEPTION_METHODS);
        }

        BigDecimal total     = reservation.getTotalPrice();
        BigDecimal deposit   = total
                .multiply(BigDecimal.valueOf(effectivePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal remaining = total.subtract(deposit);

        String validationToken = "QR-" + UUID.randomUUID().toString().toUpperCase();
        String qrPayload = buildQRPayload(reservation, validationToken, deposit, remaining, clientEmail);
        String qrBase64  = qrCodeService.generateQRCodeBase64(qrPayload);

        Payment payment = Payment.builder()
                .reservation(reservation)
                .amount(total)
                .depositAmount(deposit)
                .remainingAmount(remaining)
                .minimumDepositPercent(minimumDepositPercent)
                .method(method)
                .remainingPaymentMethod(fullPayment ? null : remainingMethod)
                .status(PaymentStatus.SUCCESS)
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now())
                .qrCodeBase64(qrBase64)
                .qrValidationToken(validationToken)
                .build();

        paymentRepository.save(payment);
        reservationService.updateStatus(reservationId, ReservationStatus.CONFIRMED);

        if (clientEmail != null && !clientEmail.isBlank())
            notificationService.sendPaymentConfirmation(payment, clientEmail);

        log.info("Deposit processed for reservation {}. Deposit: {} / Remaining: {} / Token: {}",
                reservationId, deposit, remaining, validationToken);

        return paymentMapper.toDTO(payment);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÉTHODES EXISTANTES — inchangées
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public PaymentDTO refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        if (payment.getStatus() != PaymentStatus.SUCCESS)
            throw new RuntimeException("Only successful payments can be refunded");
        payment.setStatus(PaymentStatus.REFUNDED);
        reservationService.updateStatus(payment.getReservation().getId(), ReservationStatus.CANCELLED);
        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    public QRScanResultDTO scanQRCode(String qrToken) {
        Payment payment = paymentRepository.findByQrValidationToken(qrToken).orElse(null);
        if (payment == null)
            return QRScanResultDTO.builder().valid(false)
                    .message("❌ Invalid QR code – token not found.").build();
        if (payment.getStatus() != PaymentStatus.SUCCESS)
            return QRScanResultDTO.builder().valid(false)
                    .message("❌ Payment is not in a valid state: " + payment.getStatus()).build();

        Reservation reservation = payment.getReservation();
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservationService.updateStatus(reservation.getId(), ReservationStatus.ACTIVE);
            reservation.setStatus(ReservationStatus.ACTIVE);
        }

        var spot    = reservation.getSpot();
        var camping = spot.getCamping();
        var user    = reservation.getUser();
        String remainingMethodLabel = payment.getRemainingPaymentMethod() != null
                ? payment.getRemainingPaymentMethod().name() : "FULLY PAID";

        return QRScanResultDTO.builder()
                .valid(true).message("✅ Valid reservation. Client checked in.")
                .clientId(user.getId()).clientName(user.getNom()).clientEmail(user.getEmail())
                .reservationId(reservation.getId()).reservationStatus(reservation.getStatus())
                .checkIn(reservation.getCheckIn()).checkOut(reservation.getCheckOut())
                .numberOfGuests(reservation.getNumberOfGuests())
                .campingId(camping.getId()).campingName(camping.getName())
                .spotId(spot.getId()).spotName(spot.getName())
                .totalAmount(payment.getAmount()).depositPaid(payment.getDepositAmount())
                .remainingDue(payment.getRemainingAmount())
                .transactionRef(payment.getTransactionRef())
                .remainingPaymentMethod(remainingMethodLabel).build();
    }

    @Override
    public PaymentDTO settleRemainingBalance(Long paymentId, PaymentMethod receptionMethod) {
        if (!RECEPTION_METHODS.contains(receptionMethod))
            throw new RuntimeException("Remaining balance must be settled with a reception method.");
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        if (payment.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0)
            throw new RuntimeException("No remaining balance for this payment.");
        payment.setDepositAmount(payment.getAmount());
        payment.setRemainingAmount(BigDecimal.ZERO);
        payment.setRemainingPaymentMethod(receptionMethod);
        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    @Override
    public PaymentDTO getByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .map(paymentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("No payment found for reservation: " + reservationId));
    }

    @Override
    public PaymentDTO getById(Long id) {
        return paymentRepository.findById(id)
                .map(paymentMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    @Override
    public List<PaymentDTO> getAll() {
        return paymentRepository.findAll().stream()
                .map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public void resendConfirmation(Long paymentId, String email) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        notificationService.sendPaymentConfirmation(payment, email);
    }

    private String buildQRPayload(Reservation reservation, String token,
                                  BigDecimal deposit, BigDecimal remaining, String clientEmail) {
        try {
            var spot    = reservation.getSpot();
            var camping = spot.getCamping();
            var user    = reservation.getUser();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("token",         token);
            payload.put("reservationId", reservation.getId());
            payload.put("clientName",    user.getNom());
            payload.put("clientEmail",   clientEmail);
            payload.put("campingId",     camping.getId());
            payload.put("campingName",   camping.getName());
            payload.put("spotId",        spot.getId());
            payload.put("spotName",      spot.getName());
            payload.put("checkIn",       reservation.getCheckIn().toString());
            payload.put("checkOut",      reservation.getCheckOut().toString());
            payload.put("paymentStatus", "DEPOSIT_PAID");
            payload.put("depositPaid",   deposit.toPlainString());
            payload.put("remainingDue",  remaining.toPlainString());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "https://dictation-ahead-statistic.ngrok-free.dev/camping"
                    + "?reservationId=" + reservation.getId()
                    + "&token=" + token;
            //return "TOKEN:" + token + "|RESERVATION:" + reservation.getId();
        }
    }
}