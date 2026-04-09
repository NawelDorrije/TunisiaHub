package org.example.backend_tunisiahub.Controllers.TrendyPlaces;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ReservationActiviteRepository;
import org.example.backend_tunisiahub.Services.TrendyPlaces.FactureService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;
    private final ReservationActiviteRepository reservationRepo;

    // Générer/obtenir le token d'une réservation
    @PostMapping("/generer/{reservationId}")
    public ResponseEntity<?> genererFacture(@PathVariable Long reservationId) {
        return reservationRepo.findById(reservationId)
                .map(r -> {
                    String token = factureService.genererToken(r);
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "reservationId", r.getId()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ← Endpoint public scanné par le QR code
    @GetMapping("/verify/{token}")
    public ResponseEntity<?> verifier(@PathVariable String token) {
        FactureService.VerificationResult result = factureService.verifierBillet(token);

        // Headers pour bypass ngrok warning + HTML response
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("ngrok-skip-browser-warning", "true");
        headers.setContentType(org.springframework.http.MediaType.TEXT_HTML);

        if (result.reservation() != null) {
            ReservationActivite r = result.reservation();
            String html = buildVerifyHtml(result.valide(), result.code(), result.message(),
                    r.getUser().getPrenom() + " " + r.getUser().getNom(),
                    r.getActivite().getNomActivite(),
                    r.getActivite().getLieu() != null ? r.getActivite().getLieu().getNom() : "-",
                    r.getActivite().getDateEvenement() != null ? r.getActivite().getDateEvenement().toString() : "-",
                    r.getNombrePersonnes(),
                    r.getMontantPaye() != null ? r.getMontantPaye() : r.getPrixTotal()
            );
            return new ResponseEntity<>(html, headers, org.springframework.http.HttpStatus.OK);
        }

        String html = buildVerifyHtml(false, result.code(), result.message(),
                null, null, null, null, null, null);
        return new ResponseEntity<>(html, headers, org.springframework.http.HttpStatus.OK);
    }

    private String buildVerifyHtml(boolean valide, String code, String message,
                                   String nom, String activite, String lieu, String date,
                                   Integer personnes, Double montant) {

        String color = valide ? "#28a745" : "#dc3545";
        String icon = valide ? "✅" : "❌";
        String bg = valide ? "#f0fff4" : "#fff5f5";

        StringBuilder details = new StringBuilder();
        if (nom != null) {
            details.append(detailRow("👤 Client", nom));
            details.append(detailRow("🎯 Activité", activite));
            details.append(detailRow("📍 Lieu", lieu));
            details.append(detailRow("📅 Date", date));
            details.append(detailRow("👥 Personnes", String.valueOf(personnes)));
            details.append(detailRow("💰 Montant", montant + " TND"));
        }

        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Vérification Billet</title>
          <style>
            * { box-sizing: border-box; margin: 0; padding: 0; }
            body {
              min-height: 100vh;
              display: flex;
              align-items: center;
              justify-content: center;
              background: #eef2f7;
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
              padding: 20px;
            }
            .card {
              background: white;
              border-radius: 20px;
              padding: 40px 32px;
              max-width: 420px;
              width: 100%%;
              text-align: center;
              box-shadow: 0 10px 40px rgba(0,0,0,0.12);
              border-top: 5px solid %s;
            }
            .brand { font-size: 0.75rem; color: #adb5bd; letter-spacing: 2px; margin-bottom: 24px; }
            .icon { font-size: 4rem; margin-bottom: 16px; }
            .title { font-size: 1.3rem; font-weight: 700; color: %s; margin-bottom: 6px; }
            .code { font-size: 0.7rem; letter-spacing: 3px; color: #adb5bd; margin-bottom: 24px; font-family: monospace; }
            .details { background: %s; border-radius: 12px; padding: 16px; text-align: left; }
            .row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 0.88rem; border-bottom: 1px solid rgba(0,0,0,0.06); }
            .row:last-child { border-bottom: none; }
            .row span { color: #6c757d; }
            .row strong { color: #1a1a2e; }
          </style>
        </head>
        <body>
          <div class="card">
            <div class="brand">🗺️ DISCOVER TUNISIA — TUNISIAHUB</div>
            <div class="icon">%s</div>
            <div class="title">%s</div>
            <div class="code">%s</div>
            %s
          </div>
        </body>
        </html>
        """.formatted(color, color, bg, icon, message, code,
                details.isEmpty() ? "" : "<div class='details'>" + details + "</div>");
    }

    private String detailRow(String label, String value) {
        return "<div class='row'><span>" + label + "</span><strong>" + value + "</strong></div>";
    }

}