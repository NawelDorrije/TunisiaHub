package org.example.backend_tunisiahub.Controllers.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.example.backend_tunisiahub.Services.TrendyPlaces.IReservationActiviteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations-activites")
@RequiredArgsConstructor
public class ReservationActiviteController {

    private final IReservationActiviteService service;

    // Créer une réservation
    @PostMapping("/activite/{activiteId}/user/{userId}")
    public ReservationActivite create(
            @PathVariable Long activiteId,
            @PathVariable Long userId,
            @RequestBody ReservationActivite reservation) {
        return service.createReservation(reservation, activiteId, userId);
    }

    // Réservations d'un user
    @GetMapping("/user/{userId}")
    public List<ReservationActivite> getByUser(@PathVariable Long userId) {
        return service.getReservationsByUser(userId);
    }

    // Toutes les réservations (admin)
    @GetMapping
    public List<ReservationActivite> getAll() {
        return service.getAllReservations();
    }

    // Changer statut (admin)
    @PatchMapping("/{id}/statut")
    public ReservationActivite updateStatut(
            @PathVariable Long id,
            @RequestBody StatutRequest request) {
        return service.updateStatut(id, request.statut());
    }

    // Annuler (user)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteReservation(id);
    }
    // Remplace l'ancien endpoint /payer par ces deux :

    @PostMapping("/{id}/payer")
    public ResponseEntity<?> payer(
            @PathVariable Long id,
            @RequestBody PayerRequest request) {
        try {
            ReservationActivite r = service.getById(id);
            if (r == null) return ResponseEntity.notFound().build();
            if (!r.getStatut().equals("EN_ATTENTE")) {
                return ResponseEntity.badRequest().body("Réservation déjà traitée");
            }

            ReservationActivite updated = service.payerReservation(
                    id,
                    request.modePaiement(),
                    request.nombreTranches()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statut", updated.getStatut(),
                    "modePaiement", updated.getModePaiement(),
                    "montantPaye", updated.getMontantPaye(),
                    "montantRestant", updated.getMontantRestant(),
                    "paiementComplet", updated.getPaiementComplet(),
                    "nombreTranches", updated.getNombreTranches() != null ? updated.getNombreTranches() : 1,
                    "trancheActuelle", updated.getTrancheActuelle()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/payer-tranche")
    public ResponseEntity<?> payerTranche(@PathVariable Long id) {
        try {
            ReservationActivite updated = service.payerTranche(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statut", updated.getStatut(),
                    "montantPaye", updated.getMontantPaye(),
                    "montantRestant", updated.getMontantRestant(),
                    "paiementComplet", updated.getPaiementComplet(),
                    "trancheActuelle", updated.getTrancheActuelle()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur: " + e.getMessage());
        }
    }

    private record PayerRequest(String modePaiement, Integer nombreTranches) {}
    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail() {
        try {
            // Import simple pour tester
            org.springframework.mail.SimpleMailMessage msg = new org.springframework.mail.SimpleMailMessage();
            msg.setFrom("milouchi.iyed@gmail.com");
            msg.setTo("milouchi.iyed@gmail.com");
            msg.setSubject("Test email Spring Boot");
            msg.setText("Email de test - Discover Tunisia fonctionne !");

            // Tu dois injecter JavaMailSender dans le controller pour ce test
            // OU fais-le directement dans EmailService :
            return ResponseEntity.ok("Test lancé - vérifie les logs");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    private record StatutRequest(String statut) {}
}