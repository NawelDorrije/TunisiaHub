package com.example.foyer.service;

import com.example.foyer.entities.Reservation;
import com.example.foyer.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationServiceImpl implements IReservationService {

    private ReservationRepository reservationRepository;

    public List<Reservation> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation retrieveReservation(Long idReservation) {
        return reservationRepository.findById(idReservation).get();
    }

    public Reservation addReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public void removeReservation(Long idReservation) {
        reservationRepository.deleteById(idReservation);
    }

    public Reservation modifyReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    public List<Reservation> retrieveValidReservationsByYear(LocalDate anneUniversite) {
        return reservationRepository.findByEstValideTrueAndAnneUniversite(anneUniversite);
    }
}
