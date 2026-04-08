package org.example.backend_tunisiahub.Entities.TrendyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend_tunisiahub.Entities.User.User;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReservationActivite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dateReservation;

    private Integer nombrePersonnes;
    private Double prixTotal;
    private String statut;

    @ManyToOne
    @JoinColumn(name = "activite_id")
    @JsonIgnoreProperties({"lieu", "reservations"})  // ← AJOUTE ÇA
    private ActiviteLieu activite;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"reservations", "motDePasse"})  // ← ET ÇA
    private User user;
}