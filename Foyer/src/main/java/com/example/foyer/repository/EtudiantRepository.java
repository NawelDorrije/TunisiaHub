package com.example.foyer.repository;

import com.example.foyer.entities.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    long countByDateNaissanceAfterAndEcole(LocalDate dateNaissance, String ecole);
}

