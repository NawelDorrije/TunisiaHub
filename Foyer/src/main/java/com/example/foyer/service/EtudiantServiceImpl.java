package com.example.foyer.service;

import com.example.foyer.entities.Etudiant;
import com.example.foyer.entities.Reservation;
import com.example.foyer.repository.EtudiantRepository;
import com.example.foyer.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class EtudiantServiceImpl implements IEtudiantService {

    private EtudiantRepository etudiantRepository;
    private ReservationRepository reservationRepository;

    public List<Etudiant> retrieveAllEtudiants() {
        return etudiantRepository.findAll();
    }

    public Etudiant retrieveEtudiant(Long idEtudiant) {
        return etudiantRepository.findById(idEtudiant).get();
    }

    public Etudiant addEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public void removeEtudiant(Long idEtudiant) {
        etudiantRepository.deleteById(idEtudiant);
    }

    public Etudiant modifyEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    public long countStudentsByBirthDateAndSchool(LocalDate dateNaissance, String ecole) {
        return etudiantRepository.countByDateNaissanceAfterAndEcole(dateNaissance, ecole);
    }

    @Override
    public void assignEtudiantToReservation(Long etudiantId, Long reservationId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId).get();
        Reservation reservation = reservationRepository.findById(reservationId).get();
        reservation.getEtudiants().add(etudiant);
        reservationRepository.save(reservation);
    }

    @Override
    public void desaffecterEtudiantFromReservation(Long etudiantId, Long reservationId) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId).get();
        Reservation reservation = reservationRepository.findById(reservationId).get();
        reservation.getEtudiants().remove(etudiant);
        reservationRepository.save(reservation);
    }
}
