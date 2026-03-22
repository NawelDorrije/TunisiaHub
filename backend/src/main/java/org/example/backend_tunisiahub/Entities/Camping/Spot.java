package org.example.backend_tunisiahub.Entities.Camping;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    int number;

    double size;

    boolean availability;

    double price;

    int maxCapacity;

    @ManyToOne
    @JoinColumn(name = "camping_id")
    @JsonIgnoreProperties("spots") // ignore uniquement la liste de spots pour éviter boucle
    Camping camping;

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations;

}