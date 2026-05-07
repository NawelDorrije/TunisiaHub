package org.example.backend_tunisiahub.Services.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;

import java.util.List;

public interface IReservationActiviteService {
    ReservationActivite createReservation(ReservationActivite reservation, Long activiteId, Long userId);
    List<ReservationActivite> getReservationsByUser(Long userId);
    List<ReservationActivite> getAllReservations();
    ReservationActivite updateStatut(Long id, String statut);
    void deleteReservation(Long id);
    ReservationActivite getById(Long id);
    ReservationActivite payerReservation(Long id, String modePaiement, Integer nombreTranches);
    ReservationActivite payerTranche(Long id); // payer la prochaine tranche
    ReservationActivite configurerNotification(Long id, Boolean active, Integer joursAvant);
    List<ReservationActivite> getConflits(Long userId, Long activiteId);
}