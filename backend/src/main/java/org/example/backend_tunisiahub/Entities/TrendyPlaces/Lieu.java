package org.example.backend_tunisiahub.Entities.TrendyPlaces;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String description;
    private String type;
    private String ville;
    private String image;
    private Double latitude;
    private Double longitude;
    private String horaires;

    @OneToMany(mappedBy = "lieu", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("lieu")
    private List<ActiviteLieu> activites;
}