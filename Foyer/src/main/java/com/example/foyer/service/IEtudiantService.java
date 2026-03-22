package com.example.foyer.service;

import com.example.foyer.entities.Etudiant;
import java.time.LocalDate;
import java.util.List;

public interface IEtudiantService {
    public List<Etudiant> retrieveAllEtudiants();
    public Etudiant retrieveEtudiant(Long idEtudiant);
    public Etudiant addEtudiant(Etudiant etudiant);
    public void removeEtudiant(Long idEtudiant);
    public Etudiant modifyEtudiant(Etudiant etudiant);
    public long countStudentsByBirthDateAndSchool(LocalDate dateNaissance, String ecole);

    void assignEtudiantToReservation(Long etudiantId, Long reservationId);
    void desaffecterEtudiantFromReservation(Long etudiantId, Long reservationId);
}
