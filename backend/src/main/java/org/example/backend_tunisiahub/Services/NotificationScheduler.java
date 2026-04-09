package org.example.backend_tunisiahub.Services;
//package org.example.backend_tunisiahub.Services.TrendyPlaces;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.example.backend_tunisiahub.Repositories.TrendyPlaces.ReservationActiviteRepository;
import org.example.backend_tunisiahub.Services.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ReservationActiviteRepository reservationRepo;
    private final EmailService emailService;

    // Exécuté tous les jours à 8h00 du matin
    @Scheduled(cron = "0 15 18 * * *")
    public void envoyerRappels() {
        log.info("🔔 Vérification des rappels de notifications...");

        Date aujourd_hui = new Date();
        List<ReservationActivite> aNotifier = reservationRepo.findReservationsANotifier(aujourd_hui);

        log.info("📧 {} rappel(s) à envoyer", aNotifier.size());

        for (ReservationActivite r : aNotifier) {
            try {
                emailService.sendRappelEvenement(r);
                r.setNotificationEnvoyee(true);
                reservationRepo.save(r);
                log.info("✅ Rappel envoyé à {}", r.getUser().getEmail());
            } catch (Exception e) {
                log.error("❌ Erreur rappel pour réservation {}: {}", r.getId(), e.getMessage());
            }
        }
    }

    // Pour tester : exécuté toutes les minutes (à commenter en production)
    // @Scheduled(fixedRate = 60000)
    // public void envoyerRappelsTest() { envoyerRappels(); }
}