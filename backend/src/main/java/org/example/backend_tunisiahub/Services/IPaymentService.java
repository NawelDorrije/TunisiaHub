package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Camping.DTO.PaymentDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.QRScanResultDTO;
import org.example.backend_tunisiahub.Entities.PaymentMethod;

import java.util.List;

public interface IPaymentService {

    /**
     * Processes the online deposit for a reservation.
     * <ol>
     *   <li>Validates reservation is PENDING and not yet paid.</li>
     *   <li>Applies the deposit (must meet minimum percentage).</li>
     *   <li>Generates a secure check-in QR code.</li>
     *   <li>Sends a confirmation email with the QR code to the client.</li>
     *   <li>Updates reservation status to CONFIRMED.</li>
     * </ol>
     *
     * @param reservationId       target reservation
     * @param method              online payment method for the deposit
     * @param depositPercent      percentage of total to pay now (null → use default 30%)
     * @param remainingMethod     how the remaining balance will be settled at reception
     * @param clientEmail         email address to send the confirmation to
     */
    PaymentDTO processDeposit(Long reservationId,
                              PaymentMethod method,
                              Integer depositPercent,
                              PaymentMethod remainingMethod,
                              String clientEmail);

    /**
     * Refunds the deposit and cancels the reservation.
     */
    PaymentDTO refund(Long paymentId);

    /**
     * Scans and validates a check-in QR code at reception.
     * Returns reservation details and marks the reservation as ACTIVE if valid.
     *
     * @param qrToken the validation token extracted from the QR code
     */
    QRScanResultDTO scanQRCode(String qrToken);

    /**
     * Marks the remaining balance as settled at reception.
     *
     * @param paymentId           the payment whose remaining balance was collected
     * @param receptionMethod     CASH or CARD_AT_RECEPTION
     */
    PaymentDTO settleRemainingBalance(Long paymentId, PaymentMethod receptionMethod);

    // ── Queries ──────────────────────────────────────────────────────────────

    PaymentDTO getByReservationId(Long reservationId);

    PaymentDTO getById(Long id);

    List<PaymentDTO> getAll();

    /**
     * Resends the confirmation email + QR code for an existing payment.
     */
    void resendConfirmation(Long paymentId, String email);
}