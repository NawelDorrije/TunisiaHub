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
    public ResponseEntity<?> verifier(@PathVariable String token,
                                      HttpServletResponse response) {
        // ← Bypass la page d'avertissement ngrok
        response.setHeader("ngrok-skip-browser-warning", "true");

        FactureService.VerificationResult result = factureService.verifierBillet(token);

        if (result.reservation() != null) {
            ReservationActivite r = result.reservation();
            return ResponseEntity.ok(Map.of(
                    "valide", result.valide(),
                    "code", result.code(),
                    "message", result.message(),
                    "nom", r.getUser().getPrenom() + " " + r.getUser().getNom(),
                    "email", r.getUser().getEmail(),
                    "activite", r.getActivite().getNomActivite(),
                    "lieu", r.getActivite().getLieu() != null ? r.getActivite().getLieu().getNom() : "-",
                    "dateEvenement", r.getActivite().getDateEvenement() != null
                            ? r.getActivite().getDateEvenement().toString() : "-",
                    "nombrePersonnes", r.getNombrePersonnes(),
                    "montantPaye", r.getMontantPaye() != null ? r.getMontantPaye() : r.getPrixTotal()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "valide", result.valide(),
                "code", result.code(),
                "message", result.message()
        ));
    }
}