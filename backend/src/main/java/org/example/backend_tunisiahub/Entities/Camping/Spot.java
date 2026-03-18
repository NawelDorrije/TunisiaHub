package org.example.backend_tunisiahub.Entities.Camping;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    Camping camping;

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations;

}