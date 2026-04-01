package org.example.backend_tunisiahub.Entities.Accommodation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(length = 2000)
    String description;

    @Column(nullable = false)
    String adresse;

    @Column(nullable = false)
    String type;

    @Column(nullable = false)
    double price;

    @Column(nullable = false)
    int capacite;
    @Column
    Double latitude;

    @Column
    Double longitude;

    @ElementCollection
    @CollectionTable(name = "accommodation_photos", joinColumns = @JoinColumn(name = "accommodation_id"))
    @Column(name = "photo_url")
    List<String> photos = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    List<AccommodationReview> accommodationReviews = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations = new ArrayList<>();
}
