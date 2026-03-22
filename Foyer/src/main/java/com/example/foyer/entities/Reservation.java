package com.example.foyer.entities;

import java.time.LocalDate;
import java.util.HashSet;
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
@Table(name = "reservations")
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idReservation ;
    LocalDate anneUniversite ;
    boolean estValide ;

    // Le Parent (Reservation) définit la relation ManyToMany sans mappedBy
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    Set<Etudiant> etudiants = new HashSet<>();
}
