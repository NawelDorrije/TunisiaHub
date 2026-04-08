package org.example.backend_tunisiahub.Entities.TrendyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ActiviteLieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomActivite;
    private String description;
    private Double prix;
    private Integer duree;
    private Integer capaciteMax;
    private Boolean disponible;

    @ManyToOne
    @JoinColumn(name = "lieu_id")
    @JsonIgnoreProperties({"activites", "reservations"})  // lieu n'a pas de reservations mais par sécurité
    private Lieu lieu;
}