package com.example.foyer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "foyers")
@NoArgsConstructor
public class Foyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long idFoyer ;
    String nomFoyer ;
    long capaciteFoyer ;

    @OneToOne(mappedBy = "foyer")
    @JsonIgnore
    @ToString.Exclude
    Universite universite ;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "foyer")
    Set<Bloc> blocs ;
}


