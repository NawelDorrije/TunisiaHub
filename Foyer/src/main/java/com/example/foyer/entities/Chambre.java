package com.example.foyer.entities;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chambres")
@NoArgsConstructor
public class Chambre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idChambre ;
    Long numeroChambre ;

    @Enumerated(EnumType.STRING)
    TypeChambre typeC;

    @ManyToOne
    Bloc bloc;

    @OneToMany (cascade = CascadeType.ALL)
    Set<Reservation>  reservations ;
}
