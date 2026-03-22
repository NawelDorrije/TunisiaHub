package org.example.backend_tunisiahub.Entities.Camping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Camping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String location;

    @Enumerated(EnumType.STRING)
    CampingType campingType;

    double price;

    String description;

    LocalDate startDate;

    LocalDate endDate;

    @ElementCollection
    List<String> photos = new ArrayList<>();

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("camping") // ignore la référence inverse pour éviter boucle
    List<Spot> spots;


}
