package org.example.backend_tunisiahub.Entities.TrendyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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

    // ← NOUVEAU : date de l'événement
    @Temporal(TemporalType.DATE)
    private Date dateEvenement;

    @ManyToOne
    @JoinColumn(name = "lieu_id")
    @JsonIgnoreProperties({"activites", "reservations"})
    private Lieu lieu;
    // Ajoute ce champ dans ActiviteLieu.java
    private Integer placesReservees = 0; // places déjà réservées
}