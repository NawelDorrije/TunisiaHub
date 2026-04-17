package org.example.backend_tunisiahub.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // ── Configurable minimum deposit percentage (default 30%) ─────────────────
    @Value("${app.payment.minimum-deposit-percent:30}")
    private int minimumDepositPercent;

    // ── Online-only methods allowed for the deposit ───────────────────────────
    private static final Set<PaymentMethod> ONLINE_METHODS = Set.of(
            PaymentMethod.CREDIT_CARD,
            PaymentMethod.PAYPAL,
            PaymentMethod.BANK_TRANSFER
    );

    // ── Reception-only methods allowed for remaining balance ──────────────────
    private static final Set<PaymentMethod> RECEPTION_METHODS = Set.of(
            PaymentMethod.CASH,
            PaymentMethod.CARD_AT_RECEPTION
    );

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

    // ── processDeposit ────────────────────────────────────────────────────────

    @Override
    public PaymentDTO processDeposit(Long reservationId,
                                     PaymentMethod method,
                                     Integer depositPercent,
                                     PaymentMethod remainingMethod,
                                     String clientEmail) {

        // 1. Validate payment method is an online method
        if (!ONLINE_METHODS.contains(method)) {
            throw new RuntimeException(
                    "Deposit must be paid with an online method: " + ONLINE_METHODS);
        }

        // 2. Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Only PENDING reservations can be paid. Current status: "
                    + reservation.getStatus());
        }

        // 3. Prevent duplicate payment
        if (paymentRepository.findByReservationId(reservationId).isPresent()) {
            throw new RuntimeException("A payment already exists for reservation " + reservationId);
        }

        // 4. Resolve effective deposit percentage
        int effectivePercent = (depositPercent != null) ? depositPercent : minimumDepositPercent;
        if (effectivePercent < minimumDepositPercent) {
            throw new RuntimeException(String.format(
                    "Minimum deposit is %d%% of the total. You requested %d%%.",
                    minimumDepositPercent, effectivePercent));
        }
        if (effectivePercent > 100) {
            throw new RuntimeException("Deposit percent cannot exceed 100%.");
        }

        // 5. Validate remaining-balance method when there IS a remaining balance
        boolean fullPayment = (effectivePercent == 100);
        if (!fullPayment) {
            if (remainingMethod == null) {
                throw new RuntimeException(
                        "remainingPaymentMethod is required when deposit < 100% (CASH or CARD_AT_RECEPTION)");
            }
            if (!RECEPTION_METHODS.contains(remainingMethod)) {
                throw new RuntimeException(
                        "Remaining balance must be settled at reception: " + RECEPTION_METHODS);
            }
        }

        // 6. Calculate amounts
        BigDecimal total     = reservation.getTotalPrice();
        BigDecimal deposit   = total
                .multiply(BigDecimal.valueOf(effectivePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal remaining = total.subtract(deposit);

        // 7. Generate unique QR validation token
        String validationToken = "QR-" + UUID.randomUUID().toString().toUpperCase();

        // 8. Build QR code payload (compact JSON)
        String qrPayload = buildQRPayload(
                reservation, validationToken, deposit, remaining, clientEmail);
        String qrBase64 = qrCodeService.generateQRCodeBase64(qrPayload);

        // 9. Persist payment
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

        // 10. Update reservation to CONFIRMED
        reservationService.updateStatus(reservationId, ReservationStatus.CONFIRMED);

        // 11. Send confirmation email (non-blocking – failure is logged, not thrown)
        if (clientEmail != null && !clientEmail.isBlank()) {
            notificationService.sendPaymentConfirmation(payment, clientEmail);
        }

        log.info("Deposit processed for reservation {}. Deposit: {} / Remaining: {} / Token: {}",
                reservationId, deposit, remaining, validationToken);

        return paymentMapper.toDTO(payment);
    }

    // ── refund ────────────────────────────────────────────────────────────────

    @Override
    public PaymentDTO refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        reservationService.updateStatus(payment.getReservation().getId(), ReservationStatus.CANCELLED);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    // ── scanQRCode ────────────────────────────────────────────────────────────

    @Override
    public QRScanResultDTO scanQRCode(String qrToken) {
        Payment payment = paymentRepository.findByQrValidationToken(qrToken)
                .orElse(null);

        if (payment == null) {
            return QRScanResultDTO.builder()
                    .valid(false)
                    .message("❌ Invalid QR code – token not found.")
                    .build();
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            return QRScanResultDTO.builder()
                    .valid(false)
                    .message("❌ Payment is not in a valid state: " + payment.getStatus())
                    .build();
        }

        Reservation reservation = payment.getReservation();

        // Auto-activate reservation on check-in scan
        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            reservationService.updateStatus(reservation.getId(), ReservationStatus.ACTIVE);
            reservation.setStatus(ReservationStatus.ACTIVE); // reflect in response
        }

        var spot    = reservation.getSpot();
        var camping = spot.getCamping();
        var user    = reservation.getUser();

        String remainingMethodLabel = payment.getRemainingPaymentMethod() != null
                ? payment.getRemainingPaymentMethod().name() : "FULLY PAID";

        return QRScanResultDTO.builder()
                .valid(true)
                .message("✅ Valid reservation. Client checked in.")
                .clientId(user.getId())
                .clientName(user.getNom() )
                .clientEmail(user.getEmail())
                .reservationId(reservation.getId())
                .reservationStatus(reservation.getStatus())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .numberOfGuests(reservation.getNumberOfGuests())
                .campingId(camping.getId())
                .campingName(camping.getName())
                .spotId(spot.getId())
                .spotName(spot.getName())
                .totalAmount(payment.getAmount())
                .depositPaid(payment.getDepositAmount())
                .remainingDue(payment.getRemainingAmount())
                .transactionRef(payment.getTransactionRef())
                .remainingPaymentMethod(remainingMethodLabel)
                .build();
    }

    // ── settleRemainingBalance ────────────────────────────────────────────────

    @Override
    public PaymentDTO settleRemainingBalance(Long paymentId, PaymentMethod receptionMethod) {
        if (!RECEPTION_METHODS.contains(receptionMethod)) {
            throw new RuntimeException("Remaining balance must be settled with a reception method.");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("No remaining balance for this payment.");
        }

        // Mark deposit amount = full amount, remaining = 0
        payment.setDepositAmount(payment.getAmount());
        payment.setRemainingAmount(BigDecimal.ZERO);
        payment.setRemainingPaymentMethod(receptionMethod);

        return paymentMapper.toDTO(paymentRepository.save(payment));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

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
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void resendConfirmation(Long paymentId, String email) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        notificationService.sendPaymentConfirmation(payment, email);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the JSON payload embedded in the QR code.
     * Kept minimal but complete for reception staff to verify.
     */
    private String buildQRPayload(Reservation reservation,
                                  String token,
                                  BigDecimal deposit,
                                  BigDecimal remaining,
                                  String clientEmail) {
        try {
            var spot    = reservation.getSpot();
            var camping = spot.getCamping();
            var user    = reservation.getUser();

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("token",         token);
            payload.put("reservationId", reservation.getId());
            payload.put("clientName",    user.getNom() );
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
            // Fallback: plain-text token only
            return "TOKEN:" + token + "|RESERVATION:" + reservation.getId();
        }
    }
}