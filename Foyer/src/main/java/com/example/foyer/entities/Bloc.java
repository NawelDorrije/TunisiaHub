package com.example.foyer.entities;

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
@Table(name = "blocs")
@NoArgsConstructor

public class Bloc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idBloc ;
    String nomBloc ;
    Long capaciteBloc ;

    @ManyToOne
    Foyer foyer ;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bloc")
    Set<Chambre> chambres  ;

}