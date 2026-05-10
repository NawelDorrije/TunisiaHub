package org.example.backend_tunisiahub.Entities.Camping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties({
  "hibernateLazyInitializer",
  "handler"
})
public class Spot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // Relation avec Camping
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
    name = "camping_id",
    nullable = false
  )
  @JsonIgnoreProperties({
    "spots",
    "hibernateLazyInitializer",
    "handler"
  })
  Camping camping;

  @Column(nullable = false, length = 50)
  String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  SpotType type;

  @Column(nullable = false)
  Integer capacity;

  @Column(precision = 8, scale = 2)
  BigDecimal area;

  @Column(columnDefinition = "TEXT")
  String description;

  @Column(nullable = false, precision = 10, scale = 2)
  BigDecimal basePrice;
  @Column(precision = 10, scale = 2)
  BigDecimal maxPrice;  // Optional, set by owner. null = no upper limit.
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  SpotStatus status;

  @Column(precision = 8, scale = 4)
  BigDecimal positionX;

  @Column(precision = 8, scale = 4)
  BigDecimal positionY;

  @Enumerated(EnumType.STRING)
  ViewType viewType;

  @Column(nullable = false)
  Boolean hasShade = false;

  @Column(nullable = false)
  Boolean accessibleForDisabled = false;

  @Column(nullable = false)
  Boolean active = true;

  @Column(nullable = false)
  LocalDateTime createdAt =
    LocalDateTime.now();

    /*
     ===============================
     DYNAMIC PRICING FIELDS
     ===============================
    */

  @Column(name = "dynamic_price", precision = 10, scale = 2)
  BigDecimal dynamicPrice;

  @Column(name = "last_priced_at")
  LocalDateTime lastPricedAt;

    /*
     ===============================
     RELATIONS
     ===============================
    */

  @ElementCollection
  @CollectionTable(
    name = "spot_photos",
    joinColumns =
    @JoinColumn(name = "spot_id")
  )
  List<String> photos =
    new ArrayList<>();

  @OneToMany(
    mappedBy = "spot",
    cascade = CascadeType.ALL
  )
  @JsonIgnore
  List<Reservation> reservations =
    new ArrayList<>();

  @OneToMany(
    mappedBy = "spot",
    cascade = CascadeType.ALL
  )
  @JsonIgnore
  List<Activity> activities =
    new ArrayList<>();

  @OneToMany(
    mappedBy = "spot",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @JsonIgnore
  List<Equipement> equipements =
    new ArrayList<>();

}
