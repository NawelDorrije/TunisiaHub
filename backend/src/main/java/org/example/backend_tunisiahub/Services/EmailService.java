package org.example.backend_tunisiahub.Services;   // ← Change si ton package est différent

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendConfirmationReservation(ReservationActivite reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("milouchi.iyed@gmail.com");
            helper.setTo(reservation.getUser().getEmail());
            // ← Sans emojis dans le subject
            helper.setSubject("Confirmation de votre reservation - Discover Tunisia");
            helper.setText(buildEmailHtml(reservation), true);

            mailSender.send(message);
            System.out.println("✅ Email envoyé à: " + reservation.getUser().getEmail());

        } catch (Exception e) {
            // ← Log complet pour voir l'erreur exacte
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace(); // ← IMPORTANT
        }
    }

    public void sendStatutUpdate(ReservationActivite reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("milouchi.iyed@gmail.com");
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Mise a jour de votre reservation - Discover Tunisia");
            helper.setText(buildStatutEmailHtml(reservation), true);

            mailSender.send(message);
            System.out.println("✅ Email statut envoyé à: " + reservation.getUser().getEmail());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email statut: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildEmailHtml(ReservationActivite r) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; padding: 20px;">
              <div style="background: linear-gradient(135deg, #0f3460, #1a1a2e); padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 24px;">Reservation recue !</h1>
                <p style="color: #a8c0e8; margin: 8px 0 0;">Discover Tunisia</p>
              </div>
              <div style="background: white; padding: 30px; border-radius: 0 0 12px 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
                <p style="color: #495057; font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                <p style="color: #6c757d;">Votre reservation a bien ete enregistree et est en cours de traitement.</p>
                
                <div style="background: #f8f9ff; border-left: 4px solid #0f3460; border-radius: 8px; padding: 20px; margin: 20px 0;">
                  <h3 style="color: #0f3460; margin: 0 0 15px;">Details de la reservation</h3>
                  <table style="width: 100%%; border-collapse: collapse;">
                    <tr><td style="padding: 8px 0; color: #6c757d;">Activite</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                    <tr><td style="padding: 8px 0; color: #6c757d;">Lieu</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                    <tr><td style="padding: 8px 0; color: #6c757d;">Date</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                    <tr><td style="padding: 8px 0; color: #6c757d;">Personnes</td><td style="font-weight: bold; color: #1a1a2e;">%d</td></tr>
                    <tr><td style="padding: 8px 0; color: #6c757d;">Duree</td><td style="font-weight: bold; color: #1a1a2e;">%d min</td></tr>
                  </table>
                </div>

                <div style="background: linear-gradient(135deg, #0f3460, #1a1a2e); border-radius: 8px; padding: 15px 20px; margin: 20px 0;">
                  <table style="width: 100%%;">
                    <tr>
                      <td style="color: white; font-size: 16px; font-weight: 600;">Prix total</td>
                      <td style="color: #ffd700; font-size: 22px; font-weight: 800; text-align: right;">%.2f TND</td>
                    </tr>
                  </table>
                </div>

                <div style="text-align: center; margin: 20px 0;">
                  <span style="background: #fff3cd; color: #856404; padding: 8px 20px; border-radius: 20px; font-weight: 700; font-size: 14px;">
                    Statut : EN ATTENTE DE CONFIRMATION
                  </span>
                </div>

                <p style="color: #6c757d; font-size: 14px; text-align: center; margin-top: 20px;">
                  Vous recevrez un email des que votre reservation sera confirmee par notre equipe.
                </p>
                
                <hr style="border: none; border-top: 1px solid #f0f0f0; margin: 20px 0;">
                <p style="color: #adb5bd; font-size: 12px; text-align: center;">2026 Discover Tunisia - Tous droits reserves</p>
              </div>
            </div>
        """.formatted(
                r.getUser().getPrenom(), r.getUser().getNom(),
                r.getActivite().getNomActivite(),
                r.getActivite().getLieu() != null
                        ? r.getActivite().getLieu().getNom() + " - " + r.getActivite().getLieu().getVille()
                        : "-",
                r.getDateReservation().toString(),
                r.getNombrePersonnes(),
                r.getActivite().getDuree(),
                r.getPrixTotal()
        );
    }

    private String buildStatutEmailHtml(ReservationActivite r) {
        String statut = r.getStatut();
        String couleur = statut.equals("CONFIRMEE") ? "#28a745" : "#dc3545";
        String label = statut.equals("CONFIRMEE") ? "CONFIRMEE" : "ANNULEE";
        String messageText = statut.equals("CONFIRMEE")
                ? "Votre reservation a ete <strong>confirmee</strong> par notre equipe. A tres bientot !"
                : "Votre reservation a ete <strong>annulee</strong>. Contactez-nous pour plus d informations.";

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; padding: 20px;">
              <div style="background: linear-gradient(135deg, #0f3460, #1a1a2e); padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
                <h1 style="color: white; margin: 0; font-size: 24px;">Mise a jour de reservation</h1>
                <p style="color: #a8c0e8; margin: 8px 0 0;">Discover Tunisia</p>
              </div>
              <div style="background: white; padding: 30px; border-radius: 0 0 12px 12px;">
                <p style="color: #495057; font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
                <p style="color: #6c757d;">%s</p>
                <div style="text-align: center; margin: 25px 0;">
                  <span style="background: %s; color: white; padding: 10px 28px; border-radius: 20px; font-weight: 700; font-size: 16px;">%s</span>
                </div>
                <div style="background: #f8f9ff; border-radius: 8px; padding: 16px; margin: 16px 0;">
                  <p style="margin: 4px 0; color: #6c757d;"><strong>%s</strong> - %d personnes - <strong style="color:#0f3460;">%.2f TND</strong></p>
                </div>
                <hr style="border: none; border-top: 1px solid #f0f0f0; margin: 20px 0;">
                <p style="color: #adb5bd; font-size: 12px; text-align: center;">2026 Discover Tunisia</p>
              </div>
            </div>
        """.formatted(
                r.getUser().getPrenom(), r.getUser().getNom(),
                messageText, couleur, label,
                r.getActivite().getNomActivite(),
                r.getNombrePersonnes(),
                r.getPrixTotal()
        );
    }
    public void sendConfirmationPaiement(ReservationActivite reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("milouchi.iyed@gmail.com");
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Paiement confirme - Discover Tunisia");
            helper.setText(buildPaiementEmailHtml(reservation), true);

            mailSender.send(message);
            System.out.println("✅ Email paiement envoyé à: " + reservation.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("❌ Erreur email paiement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildPaiementEmailHtml(ReservationActivite r) {
        return """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f8f9fa; padding: 20px;">
          <div style="background: linear-gradient(135deg, #28a745, #20c997); padding: 30px; border-radius: 12px 12px 0 0; text-align: center;">
            <h1 style="color: white; margin: 0; font-size: 28px;">Paiement recu !</h1>
            <p style="color: #d4f5e2; margin: 8px 0 0; font-size: 15px;">Discover Tunisia</p>
          </div>
          <div style="background: white; padding: 30px; border-radius: 0 0 12px 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
            <p style="color: #495057; font-size: 16px;">Bonjour <strong>%s %s</strong>,</p>
            <p style="color: #6c757d;">Votre paiement a bien ete recu. Votre reservation est en attente de confirmation par notre equipe.</p>

            <div style="background: #f8f9ff; border-left: 4px solid #28a745; border-radius: 8px; padding: 20px; margin: 20px 0;">
              <h3 style="color: #28a745; margin: 0 0 15px;">Details de la reservation</h3>
              <table style="width: 100%%; border-collapse: collapse;">
                <tr><td style="padding: 8px 0; color: #6c757d;">Activite</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                <tr><td style="padding: 8px 0; color: #6c757d;">Lieu</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                <tr><td style="padding: 8px 0; color: #6c757d;">Date reservation</td><td style="font-weight: bold; color: #1a1a2e;">%s</td></tr>
                <tr><td style="padding: 8px 0; color: #6c757d;">Personnes</td><td style="font-weight: bold; color: #1a1a2e;">%d</td></tr>
              </table>
            </div>

            <div style="background: linear-gradient(135deg, #28a745, #20c997); border-radius: 8px; padding: 18px 20px; margin: 20px 0;">
              <table style="width: 100%%;">
                <tr>
                  <td style="color: white; font-size: 16px; font-weight: 600;">Montant paye</td>
                  <td style="color: #ffd700; font-size: 24px; font-weight: 800; text-align: right;">%.2f TND</td>
                </tr>
              </table>
            </div>

            <div style="text-align: center; margin: 20px 0; padding: 16px; background: #fff8e1; border-radius: 10px;">
              <p style="color: #856404; margin: 0; font-size: 14px; font-weight: 600;">
                Votre reservation est EN ATTENTE DE CONFIRMATION.
              </p>
              <p style="color: #856404; margin: 8px 0 0; font-size: 13px;">
                Vous recevrez un email de confirmation des que notre equipe valide votre demande.
              </p>
            </div>

            <hr style="border: none; border-top: 1px solid #f0f0f0; margin: 20px 0;">
            <p style="color: #adb5bd; font-size: 12px; text-align: center;">2026 Discover Tunisia - Tous droits reserves</p>
          </div>
        </div>
    """.formatted(
                r.getUser().getPrenom(), r.getUser().getNom(),
                r.getActivite().getNomActivite(),
                r.getActivite().getLieu() != null
                        ? r.getActivite().getLieu().getNom() + " - " + r.getActivite().getLieu().getVille()
                        : "-",
                r.getDateReservation().toString(),
                r.getNombrePersonnes(),
                r.getPrixTotal()
        );
    }
    public void sendConfirmationPaiementTotal(ReservationActivite reservation) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("milouchi.iyed@gmail.com");
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Reservation confirmee - Discover Tunisia");
            helper.setText(buildPaiementTotalHtml(reservation), true);
            mailSender.send(message);
            System.out.println("✅ Email paiement total envoyé");
        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    public void sendConfirmationPaiementTranche(ReservationActivite reservation, int numTranche) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("milouchi.iyed@gmail.com");
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Tranche " + numTranche + " recue - Discover Tunisia");
            helper.setText(buildPaiementTrancheHtml(reservation, numTranche), true);
            mailSender.send(message);
            System.out.println("✅ Email tranche " + numTranche + " envoyé");
        } catch (Exception e) {
            System.err.println("❌ " + e.getMessage());
        }
    }

    private String buildPaiementTotalHtml(ReservationActivite r) {
        return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;padding:20px;">
          <div style="background:#28a745;padding:30px;border-radius:12px 12px 0 0;text-align:center;">
            <h1 style="color:white;margin:0;font-size:26px;">Reservation confirmee !</h1>
            <p style="color:#d4f5e2;margin:8px 0 0;">Paiement integral recu - Discover Tunisia</p>
          </div>
          <div style="background:white;padding:30px;border-radius:0 0 12px 12px;">
            <p style="color:#495057;font-size:16px;">Bonjour <strong>%s %s</strong>,</p>
            <p style="color:#6c757d;">Votre paiement integral a ete recu. Votre reservation est <strong style="color:#28a745;">automatiquement confirmee</strong> !</p>
            <div style="background:#f8f9ff;border-left:4px solid #28a745;border-radius:8px;padding:20px;margin:20px 0;">
              <table style="width:100%%;border-collapse:collapse;">
                <tr><td style="padding:8px 0;color:#6c757d;">Activite</td><td style="font-weight:bold;color:#1a1a2e;">%s</td></tr>
                <tr><td style="padding:8px 0;color:#6c757d;">Lieu</td><td style="font-weight:bold;color:#1a1a2e;">%s</td></tr>
                <tr><td style="padding:8px 0;color:#6c757d;">Personnes</td><td style="font-weight:bold;color:#1a1a2e;">%d</td></tr>
              </table>
            </div>
            <div style="background:#28a745;border-radius:8px;padding:16px 20px;margin:20px 0;">
              <table style="width:100%%;"><tr>
                <td style="color:white;font-size:16px;font-weight:600;">Montant paye</td>
                <td style="color:#ffd700;font-size:22px;font-weight:800;text-align:right;">%.2f TND</td>
              </tr></table>
            </div>
            <div style="text-align:center;background:#d4edda;border-radius:10px;padding:16px;margin:16px 0;">
              <p style="color:#155724;font-weight:700;font-size:15px;margin:0;">Statut : CONFIRMEE</p>
            </div>
            <p style="color:#adb5bd;font-size:12px;text-align:center;">2026 Discover Tunisia</p>
          </div>
        </div>
    """.formatted(
                r.getUser().getPrenom(), r.getUser().getNom(),
                r.getActivite().getNomActivite(),
                r.getActivite().getLieu() != null ? r.getActivite().getLieu().getNom() : "-",
                r.getNombrePersonnes(),
                r.getPrixTotal()
        );
    }

    private String buildPaiementTrancheHtml(ReservationActivite r, int numTranche) {
        double montantTranche = r.getMontantPaye() / numTranche;
        boolean derniereTranche = r.getPaiementComplet();

        return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f8f9fa;padding:20px;">
          <div style="background:#0f3460;padding:30px;border-radius:12px 12px 0 0;text-align:center;">
            <h1 style="color:white;margin:0;font-size:24px;">Tranche %d recue</h1>
            <p style="color:#a8c0e8;margin:8px 0 0;">Discover Tunisia</p>
          </div>
          <div style="background:white;padding:30px;border-radius:0 0 12px 12px;">
            <p style="color:#495057;font-size:16px;">Bonjour <strong>%s %s</strong>,</p>
            <p style="color:#6c757d;">La tranche %d/%d de votre reservation a bien ete recue.</p>
            <div style="background:#f8f9ff;border-radius:8px;padding:20px;margin:20px 0;">
              <table style="width:100%%;border-collapse:collapse;">
                <tr><td style="padding:8px 0;color:#6c757d;">Activite</td><td style="font-weight:bold;">%s</td></tr>
                <tr><td style="padding:8px 0;color:#6c757d;">Montant paye</td><td style="font-weight:bold;color:#28a745;">%.2f TND</td></tr>
                <tr><td style="padding:8px 0;color:#6c757d;">Montant restant</td><td style="font-weight:bold;color:#dc3545;">%.2f TND</td></tr>
                <tr><td style="padding:8px 0;color:#6c757d;">Total</td><td style="font-weight:bold;">%.2f TND</td></tr>
              </table>
            </div>
            <div style="text-align:center;background:#fff8e1;border-radius:10px;padding:16px;">
              <p style="color:#856404;font-weight:700;margin:0;">%s</p>
            </div>
            <p style="color:#adb5bd;font-size:12px;text-align:center;margin-top:20px;">2026 Discover Tunisia</p>
          </div>
        </div>
    """.formatted(
                numTranche,
                r.getUser().getPrenom(), r.getUser().getNom(),
                numTranche, r.getNombreTranches(),
                r.getActivite().getNomActivite(),
                r.getMontantPaye(),
                r.getMontantRestant(),
                r.getPrixTotal(),
                derniereTranche ? "Toutes les tranches payees - Reservation CONFIRMEE !"
                        : "Il reste " + r.getMontantRestant() + " TND a payer"
        );
    }
}

