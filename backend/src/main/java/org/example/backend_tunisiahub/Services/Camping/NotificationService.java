package org.example.backend_tunisiahub.Services.Camping;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Payment;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * Sends HTML email notifications for payment confirmations.
 *
 * <p>Configuration required in application.properties:
 * <pre>
 * spring.mail.host=smtp.gmail.com
 * spring.mail.port=587
 * spring.mail.username=your-email@gmail.com
 * spring.mail.password=your-app-password
 * spring.mail.properties.mail.smtp.auth=true
 * spring.mail.properties.mail.smtp.starttls.enable=true
 * app.mail.from=noreply@tunisiahub.com
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender ;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DT_FMT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Sends a payment-confirmation email including:
     * <ul>
     *   <li>Full payment breakdown (deposit / remaining)</li>
     *   <li>Reservation details</li>
     *   <li>Camping / spot info</li>
     *   <li>Embedded QR code for check-in</li>
     * </ul>
     */
    public void sendPaymentConfirmation(Payment payment, String recipientEmail) {
        try {
            Reservation reservation = payment.getReservation();
            Spot         spot       = reservation.getSpot();
            Camping      camping    = spot.getCamping();

            long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());

            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart, true = utf-8
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@tunisiahub.com");
            helper.setTo(recipientEmail);
            helper.setSubject("✅ Booking Confirmed – " + camping.getName()
                    + " | Ref: " + payment.getTransactionRef());

            String html = buildEmailHtml(payment, reservation, spot, camping, nights);
            helper.setText(html, true);

            // Attach QR code inline so it renders in the email body
            if (payment.getQrCodeBase64() != null) {
                byte[] qrBytes = Base64.getDecoder().decode(payment.getQrCodeBase64());
                helper.addInline("qrcode",
                        new jakarta.mail.util.ByteArrayDataSource(qrBytes, "image/png"));
            }

            mailSender.send(message);
            log.info("Payment confirmation email sent to {} for reservation {}",
                    recipientEmail, reservation.getId());

        } catch (Exception e) {
            // Non-fatal: log and continue — payment is already saved
            log.error("Failed to send payment confirmation email to {}: {}", recipientEmail, e.getMessage(), e);
        }
    }

    // ── HTML builder ──────────────────────────────────────────────────────────

    private String buildEmailHtml(Payment payment, Reservation reservation,
                                  Spot spot, Camping camping, long nights) {

        BigDecimal deposit   = payment.getDepositAmount();
        BigDecimal remaining = payment.getRemainingAmount();
        BigDecimal total     = payment.getAmount();

        String notesHtml = (reservation.getNotes() != null && !reservation.getNotes().isBlank())
                ? "<h2>📝 Notes</h2><p style='font-size:14px;'>" + reservation.getNotes() + "</p>"
                : "";

        String remainingMethodStr = payment.getRemainingPaymentMethod() != null
                ? friendlyMethod(payment.getRemainingPaymentMethod())
                : "To be decided";

        String paidAtStr = payment.getPaidAt() != null
                ? payment.getPaidAt().format(DT_FMT)
                : "—";

        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\"/>\n" +
                "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>\n" +
                "  <title>Booking Confirmation</title>\n" +
                "  <style>\n" +
                "    body{margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;color:#333}\n" +
                "    .wrapper{max-width:640px;margin:30px auto;background:#fff;border-radius:10px;\n" +
                "             box-shadow:0 4px 20px rgba(0,0,0,.08);overflow:hidden}\n" +
                "    .header{background:linear-gradient(135deg,#1a6b3c,#2ecc71);padding:36px 30px;text-align:center}\n" +
                "    .header h1{margin:0;color:#fff;font-size:26px;letter-spacing:.5px}\n" +
                "    .header p{margin:6px 0 0;color:rgba(255,255,255,.85);font-size:14px}\n" +
                "    .body{padding:30px 36px}\n" +
                "    h2{font-size:17px;color:#1a6b3c;border-bottom:2px solid #e8f5ee;\n" +
                "       padding-bottom:8px;margin-bottom:16px}\n" +
                "    table{width:100%;border-collapse:collapse;margin-bottom:20px;font-size:14px}\n" +
                "    td{padding:9px 12px;border-bottom:1px solid #f0f0f0}\n" +
                "    tr:last-child td{border-bottom:none}\n" +
                "    .label{color:#777;width:55%}\n" +
                "    .value{font-weight:600;text-align:right}\n" +
                "    .highlight{background:#f0faf4;border-radius:8px;padding:14px 18px;\n" +
                "               margin-bottom:22px;font-size:14px}\n" +
                "    .highlight .row{display:flex;justify-content:space-between;margin-bottom:6px}\n" +
                "    .total-row .value{color:#1a6b3c;font-size:16px}\n" +
                "    .paid-row .value{color:#27ae60}\n" +
                "    .due-row .value{color:#e67e22}\n" +
                "    .qr-section{text-align:center;background:#f9f9f9;border-radius:8px;\n" +
                "                padding:24px;margin-bottom:22px}\n" +
                "    .qr-section img{width:200px;height:200px;border:4px solid #e0e0e0;border-radius:8px}\n" +
                "    .qr-section p{margin:10px 0 0;font-size:13px;color:#777}\n" +
                "    .badge{display:inline-block;background:#e8f5ee;color:#1a6b3c;\n" +
                "           border-radius:20px;padding:3px 12px;font-size:12px;font-weight:700}\n" +
                "    .footer{background:#f4f6f8;padding:18px 30px;text-align:center;\n" +
                "            font-size:12px;color:#999}\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"wrapper\">\n" +
                "    <div class=\"header\">\n" +
                "      <h1>🏕️ Booking Confirmed!</h1>\n" +
                "      <p>Your camping reservation is secured. See you soon!</p>\n" +
                "    </div>\n" +
                "    <div class=\"body\">\n" +
                "      <h2>💳 Payment Summary</h2>\n" +
                "      <div class=\"highlight\">\n" +
                "        <div class=\"row total-row\"><span>Total reservation amount</span><span class=\"value\">" + total.toPlainString() + " TND</span></div>\n" +
                "        <div class=\"row paid-row\"><span>✅ Deposit paid online</span><span class=\"value\">" + deposit.toPlainString() + " TND</span></div>\n" +
                "        <div class=\"row due-row\"><span>⏳ Remaining balance (at reception)</span><span class=\"value\">" + remaining.toPlainString() + " TND</span></div>\n" +
                "      </div>\n" +
                "      <table>\n" +
                "        <tr><td class=\"label\">Payment method (deposit)</td><td class=\"value\">" + friendlyMethod(payment.getMethod()) + "</td></tr>\n" +
                "        <tr><td class=\"label\">Transaction reference</td><td class=\"value\">" + payment.getTransactionRef() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Payment date</td><td class=\"value\">" + paidAtStr + "</td></tr>\n" +
                "        <tr><td class=\"label\">Remaining balance method</td><td class=\"value\">" + remainingMethodStr + "</td></tr>\n" +
                "        <tr><td class=\"label\">Status</td><td class=\"value\"><span class=\"badge\">CONFIRMED</span></td></tr>\n" +
                "      </table>\n" +
                "      <h2>📅 Reservation Details</h2>\n" +
                "      <table>\n" +
                "        <tr><td class=\"label\">Reservation ID</td><td class=\"value\">#" + reservation.getId() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Check-in</td><td class=\"value\">" + reservation.getCheckIn().format(DATE_FMT) + "</td></tr>\n" +
                "        <tr><td class=\"label\">Check-out</td><td class=\"value\">" + reservation.getCheckOut().format(DATE_FMT) + "</td></tr>\n" +
                "        <tr><td class=\"label\">Duration</td><td class=\"value\">" + nights + " night" + (nights == 1 ? "" : "s") + "</td></tr>\n" +
                "        <tr><td class=\"label\">Number of guests</td><td class=\"value\">" + reservation.getNumberOfGuests() + "</td></tr>\n" +
                "      </table>\n" +
                "      <h2>🌲 Camping Details</h2>\n" +
                "      <table>\n" +
                "        <tr><td class=\"label\">Camping</td><td class=\"value\">" + camping.getName() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Address</td><td class=\"value\">" + camping.getAddress() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Spot name</td><td class=\"value\">" + spot.getName() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Spot capacity</td><td class=\"value\">" + spot.getCapacity() + " person" + (spot.getCapacity() == 1 ? "" : "s") + "</td></tr>\n" +
                "        <tr><td class=\"label\">Check-in time</td><td class=\"value\">" + camping.getCheckInTime() + "</td></tr>\n" +
                "        <tr><td class=\"label\">Check-out time</td><td class=\"value\">" + camping.getCheckOutTime() + "</td></tr>\n" +
                "      </table>\n" +
                "      <h2>📲 Your Check-in QR Code</h2>\n" +
                "      <div class=\"qr-section\">\n" +
                "        <img src=\"cid:qrcode\" alt=\"Check-in QR Code\"/>\n" +
                "        <p>Present this QR code at the reception upon arrival.<br/>\n" +
                "           Staff will scan it to verify your reservation instantly.</p>\n" +
                "      </div>\n" +
                "      " + notesHtml + "\n" +
                "    </div>\n" +
                "    <div class=\"footer\">\n" +
                "      <p>Questions? Contact us at support@tunisiahub.com</p>\n" +
                "      <p>© 2025 TunisiaHub – Your gateway to Tunisian camping</p>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String friendlyMethod(org.example.backend_tunisiahub.Entities.PaymentMethod m) {
        return switch (m) {
            case CREDIT_CARD      -> "Credit Card";
            case PAYPAL           -> "PayPal";
            case BANK_TRANSFER    -> "Bank Transfer";
            case CASH             -> "Cash (at reception)";
            case CARD_AT_RECEPTION -> "Card (at reception)";
        };
    }
}
