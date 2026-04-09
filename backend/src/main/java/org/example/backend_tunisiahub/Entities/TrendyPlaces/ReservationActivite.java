package org.example.backend_tunisiahub.Entities.TrendyPlaces;

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
    private String statut; // EN_ATTENTE, PAYEE, CONFIRMEE, ANNULEE

    // ===== NOUVEAUX CHAMPS PAIEMENT =====
    private String modePaiement;     // TOTAL ou TRANCHE
    private Double montantPaye;      // montant déjà payé
    private Double montantRestant;   // reste à payer
    private Integer nombreTranches;  // 2 ou 3
    private Integer trancheActuelle; // 1, 2, 3...
    private Boolean paiementComplet; // true si tout est payé

    @ManyToOne
    @JoinColumn(name = "activite_id")
    private ActiviteLieu activite;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}