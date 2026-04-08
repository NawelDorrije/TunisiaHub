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
    private final EmailService emailService; // ← AJOUTE

    @Override
    public ReservationActivite createReservation(ReservationActivite reservation,
                                                 Long activiteId, Long userId) {
        ActiviteLieu activite = activiteRepo.findById(activiteId)
                .orElseThrow(() -> new RuntimeException("Activité non trouvée"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // ← Log pour vérifier
        System.out.println("👤 User email: " + user.getEmail());
        System.out.println("🎯 Activite: " + activite.getNomActivite());

        reservation.setActivite(activite);
        reservation.setUser(user);
        reservation.setDateReservation(new Date());
        reservation.setStatut("EN_ATTENTE");
        double prix = activite.getPrix() != null ? activite.getPrix() : 0.0;
        reservation.setPrixTotal(prix * reservation.getNombrePersonnes());

        ReservationActivite saved = reservationRepo.save(reservation);
        emailService.sendConfirmationReservation(saved);
        return saved;
    }

    @Override
    public ReservationActivite updateStatut(Long id, String statut) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        r.setStatut(statut);
        ReservationActivite saved = reservationRepo.save(r);

        // Email seulement si CONFIRMEE ou ANNULEE
        if (statut.equals("CONFIRMEE") || statut.equals("ANNULEE")) {
            emailService.sendStatutUpdate(saved); // ← AJOUTE
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
    public void deleteReservation(Long id) {
        reservationRepo.deleteById(id);
    }

    @Override
    public ReservationActivite getById(Long id) {
        return reservationRepo.findById(id).orElse(null);
    }
    @Override
    public ReservationActivite payerReservation(Long id) {
        ReservationActivite r = reservationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation non trouvee"));

        // Marquer comme PAYEE (nouveau statut)
        r.setStatut("PAYEE");
        ReservationActivite saved = reservationRepo.save(r);

        // Envoyer email paiement
        emailService.sendConfirmationPaiement(saved);

        return saved;
    }
}