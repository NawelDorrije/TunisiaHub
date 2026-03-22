package com.example.foyer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "etudiants")
@NoArgsConstructor

public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idEtudiant;
    String nomEt;
    String prenomEt;
    Long cin ;
    String ecole ;
    LocalDate dateNaissance;

    // "L'étudiant est le fils" => Il porte le mappedBy
    @ManyToMany(mappedBy = "etudiants", cascade = CascadeType.ALL)
    @JsonIgnore
    Set<Reservation> reservations ;
}

