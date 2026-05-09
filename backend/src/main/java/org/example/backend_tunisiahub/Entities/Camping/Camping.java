package org.example.backend_tunisiahub.Entities.Camping;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


import java.time.LocalDate;

import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;
import org.example.backend_tunisiahub.Entities.User.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

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




    String location;

    @Enumerated(EnumType.STRING)
    CampingType campingType;




    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String address;

    @Column(nullable = false)
    String governorate;

    @Column(nullable = false, precision = 9, scale = 6)
    BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    BigDecimal longitude;

    @Column(precision = 3, scale = 2, columnDefinition = "DECIMAL(3,2) DEFAULT 0.00")
    BigDecimal averageRating = BigDecimal.valueOf(0.00);

    @Column(columnDefinition = "int default 0")
    Integer numberOfSpots = 0;

    @Column(nullable = false)
    Integer maxCapacity;

    @Column(nullable = false, precision = 10, scale = 2, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    CampingStatus status;

    @Column(columnDefinition = "TEXT")
    String rules;

    @Column(columnDefinition = "TIME default '14:00'")
    LocalTime checkInTime = LocalTime.of(14, 0);

    @Column(columnDefinition = "TIME default '11:00'")
    LocalTime checkOutTime = LocalTime.of(11, 0);

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;

    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    LocalDateTime updatedAt = LocalDateTime.now();



    @Column(columnDefinition = "TEXT")
    String description;

    LocalDate startDate;

    LocalDate endDate;

    @ElementCollection
    List<String> photos = new ArrayList<>();
    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("camping") // évite boucle JSON
    List<Spot> spots = new ArrayList<>();

    @OneToMany(mappedBy = "camping", cascade = CascadeType.ALL)
    private List<Activity> activities = new ArrayList<>();

}
