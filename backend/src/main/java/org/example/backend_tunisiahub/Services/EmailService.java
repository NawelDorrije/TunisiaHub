package org.example.backend_tunisiahub.Services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final ReservationRestaurantPdfService reservationPdfService;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Async
    public void sendReservationRequest(ReservationRestaurant reservation) {
        try {
            if (!isMailConfigured()) {
                log.warn("Skipping reservation request email because MAIL_USERNAME or MAIL_PASSWORD is not configured");
                return;
            }
            String recipient = extractRecipient(reservation);
            if (!StringUtils.hasText(recipient)) return;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            if (StringUtils.hasText(mailFrom)) helper.setFrom(mailFrom);
            helper.setTo(recipient);
            helper.setSubject("Reservation Request Received #" + reservation.getId());
            helper.setText(buildRequestMailBody(reservation), false);

            mailSender.send(message);
            log.info("Reservation request email sent to {}", recipient);
        } catch (Exception ex) {
            log.error("Failed to send reservation request email", ex);
        }
    }

    @Async
    public void sendReservationConfirmation(ReservationRestaurant reservation) {
        try {
            if (!isMailConfigured()) {
                log.warn("Skipping reservation confirmation email because MAIL_USERNAME or MAIL_PASSWORD is not configured");
                return;
            }
            String recipient = extractRecipient(reservation);
            if (!StringUtils.hasText(recipient)) {
                log.warn("Skipping reservation confirmation email for reservation {} because user email is missing", reservation != null ? reservation.getId() : null);
                return;
            }

            byte[] pdfBytes = reservationPdfService.generateReservationPdf(reservation);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            if (StringUtils.hasText(mailFrom)) {
                helper.setFrom(mailFrom);
            }
            helper.setTo(recipient);
            helper.setSubject("Reservation Confirmation #" + reservation.getId());
            helper.setText(buildConfirmationMailBody(reservation), false);
            helper.addAttachment(
                    "reservation-" + reservation.getId() + ".pdf",
                    new ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
            log.info("Reservation confirmation email (with PDF) sent to {}", recipient);
        } catch (Exception ex) {
            log.error("Failed to send reservation confirmation email", ex);
        }
    }

    private String extractRecipient(ReservationRestaurant reservation) {
        if (reservation == null) {
            return null;
        }
        User user = reservation.getUser();
        return user != null ? user.getEmail() : null;
    }

    private String buildRequestMailBody(ReservationRestaurant reservation) {
        String clientName = getClientName(reservation);
        String restaurantName = getRestaurantName(reservation);

        return "Hello " + clientName + ",\n\n"
                + "We have received your reservation request for " + restaurantName + ".\n"
                + "Our team is currently reviewing it and you will receive a confirmation email once accepted.\n\n"
                + "Details:\n"
                + "- Date: " + (reservation.getDateTime() != null ? reservation.getDateTime().toString().replace("T", " ") : "-") + "\n"
                + "- Guests: " + (reservation.getPartySize() != null ? reservation.getPartySize() : "-") + "\n\n"
                + "Regards,\nTunisiaHub";
    }

    private String buildConfirmationMailBody(ReservationRestaurant reservation) {
        String clientName = getClientName(reservation);
        String restaurantName = getRestaurantName(reservation);

        return "Hello " + clientName + ",\n\n"
                + "Your reservation at " + restaurantName + " has been confirmed!\n"
                + "Please find your confirmation PDF attached to this email.\n\n"
                + "We look forward to seeing you.\n\n"
                + "Regards,\nTunisiaHub";
    }

    private String getClientName(ReservationRestaurant reservation) {
        if (reservation != null && reservation.getUser() != null) {
            String prenom = reservation.getUser().getPrenom() == null ? "" : reservation.getUser().getPrenom().trim();
            String nom = reservation.getUser().getNom() == null ? "" : reservation.getUser().getNom().trim();
            String fullName = (prenom + " " + nom).trim();
            if (StringUtils.hasText(fullName)) return fullName;
            if (StringUtils.hasText(reservation.getUser().getEmail())) return reservation.getUser().getEmail();
        }
        return "Customer";
    }

    private String getRestaurantName(ReservationRestaurant reservation) {
        return reservation != null && reservation.getRestaurant() != null
                && StringUtils.hasText(reservation.getRestaurant().getName())
                ? reservation.getRestaurant().getName().trim()
                : "our restaurant";
    }

    private boolean isMailConfigured() {
        return StringUtils.hasText(mailUsername) && StringUtils.hasText(mailPassword);
    }
}
