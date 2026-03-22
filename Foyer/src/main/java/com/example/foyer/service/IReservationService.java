package com.example.foyer.service;

import com.example.foyer.entities.Reservation;
import java.time.LocalDate;
import java.util.List;

public interface IReservationService {
    public List<Reservation> retrieveAllReservations();
    public Reservation retrieveReservation(Long idReservation);
    public Reservation addReservation(Reservation reservation);
    public void removeReservation(Long idReservation);
    public Reservation modifyReservation(Reservation reservation);
    public List<Reservation> retrieveValidReservationsByYear(LocalDate anneUniversite);
}
