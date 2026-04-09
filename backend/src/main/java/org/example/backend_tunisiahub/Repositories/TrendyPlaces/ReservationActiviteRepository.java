package org.example.backend_tunisiahub.Repositories.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationActiviteRepository extends JpaRepository<ReservationActivite, Long> {
    List<ReservationActivite> findByUserId(Long userId);

    // Trouve les réservations à notifier aujourd'hui
    @Query("""
        SELECT r FROM ReservationActivite r
        WHERE r.notificationActive = true
        AND r.notificationEnvoyee = false
        AND r.statut IN ('CONFIRMEE', 'PAYEE')
        AND r.activite.dateEvenement IS NOT NULL
        AND DATEDIFF(r.activite.dateEvenement, :today) = r.notificationJoursAvant
    """)
    List<ReservationActivite> findReservationsANotifier(@Param("today") Date today);
}