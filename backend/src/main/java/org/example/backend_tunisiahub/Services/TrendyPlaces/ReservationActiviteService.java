package org.example.backend_tunisiahub.Services.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ActiviteLieu;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ActiviteLieuRepository;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ReservationActiviteRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Services.EmailService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationActiviteService implements IReservationActiviteService {

    private final ReservationActiviteRepository reservationRepo;
    private final ActiviteLieuRepository activiteRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public ReservationActivite createReservation(ReservationActivite reservation,
                                                 Long activiteId, Long userId) {
        ActiviteLieu activite = activiteRepo.findById(activiteId)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        System.out.println("👤 User email: " + user.getEmail());

        reservation.setActivite(activite);
        reservation.setUser(user);
        reservation.setDateReservation(new Date());
        reservation.setStatut("EN_ATTENTE");
        double prix = activite.getPrix() != null ? activite.getPrix() : 0.0;
        reservation.setPrixTotal(prix * reservation.getNombrePersonnes());

        // Init champs paiement
        reservation.setMontantPaye(0.0);
        reservation.setMontantRestant(reservation.getPrixTotal());
        reservation.setPaiementComplet(false);
        reservation.setModePaiement(null);
        reservation.setNombreTranches(null);
        reservation.setTrancheActuelle(0);

        ReservationActivite saved = reservationRepo.save(reservation);
        emailService.sendConfirmationReservation(saved);
        return saved;
    }

    @Override
    public ReservationActivite payerReservation(Long id, String modePaiement, Integer nombreTranches) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        r.setModePaiement(modePaiement);

        if ("TOTAL".equals(modePaiement)) {
            // Paiement total → confirmé directement
            r.setMontantPaye(r.getPrixTotal());
            r.setMontantRestant(0.0);
            r.setPaiementComplet(true);
            r.setNombreTranches(1);
            r.setTrancheActuelle(1);
            r.setStatut("CONFIRMEE"); // ← Confirmation automatique

        } else if ("TRANCHE".equals(modePaiement)) {
            // Paiement en tranches
            int nb = (nombreTranches != null) ? nombreTranches : 2;
            r.setNombreTranches(nb);
            r.setTrancheActuelle(1);

            double montantTranche = Math.round((r.getPrixTotal() / nb) * 100.0) / 100.0;
            r.setMontantPaye(montantTranche);
            r.setMontantRestant(r.getPrixTotal() - montantTranche);
            r.setPaiementComplet(false);
            r.setStatut("PAYEE"); // Partiellement payé → en attente du reste
        }

        ReservationActivite saved = reservationRepo.save(r);

        // Email selon le mode
        if ("TOTAL".equals(modePaiement)) {
            emailService.sendConfirmationPaiementTotal(saved);
        } else {
            emailService.sendConfirmationPaiementTranche(saved, 1);
        }

        return saved;
    }

    @Override
    public ReservationActivite payerTranche(Long id) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        if (r.getPaiementComplet()) {
            throw new RuntimeException("Paiement déjà complet");
        }

        int tranchesSur = r.getNombreTranches() != null ? r.getNombreTranches() : 2;
        double montantTranche = Math.round((r.getPrixTotal() / tranchesSur) * 100.0) / 100.0;
        int nouvelleTranche = r.getTrancheActuelle() + 1;

        double nouveauMontantPaye = r.getMontantPaye() + montantTranche;
        r.setMontantPaye(Math.min(nouveauMontantPaye, r.getPrixTotal()));
        r.setMontantRestant(Math.max(r.getPrixTotal() - nouveauMontantPaye, 0.0));
        r.setTrancheActuelle(nouvelleTranche);

        if (nouvelleTranche >= tranchesSur) {
            // Toutes les tranches payées → confirmation automatique
            r.setPaiementComplet(true);
            r.setMontantRestant(0.0);
            r.setStatut("CONFIRMEE");
            emailService.sendConfirmationPaiementTotal(r);
        } else {
            emailService.sendConfirmationPaiementTranche(r, nouvelleTranche);
        }

        return reservationRepo.save(r);
    }

    @Override
    public ReservationActivite updateStatut(Long id, String statut) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        r.setStatut(statut);
        ReservationActivite saved = reservationRepo.save(r);
        if (statut.equals("CONFIRMEE") || statut.equals("ANNULEE")) {
            emailService.sendStatutUpdate(saved);
        }
        return saved;
    }

    @Override
    public List<ReservationActivite> getReservationsByUser(Long userId) {
        return reservationRepo.findByUserId(userId);
    }

    @Override
    public List<ReservationActivite> getAllReservations() {
        return reservationRepo.findAll();
    }

    @Override
    public ReservationActivite getById(Long id) {
        return reservationRepo.findById(id).orElse(null);
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepo.deleteById(id);
    }
    @Override
    public ReservationActivite configurerNotification(Long id, Boolean active, Integer joursAvant) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        r.setNotificationActive(active);
        r.setNotificationJoursAvant(joursAvant);
        r.setNotificationEnvoyee(false); // reset si on reconfigure

        return reservationRepo.save(r);
    }
}