package org.example.backend_tunisiahub.Entities.Carpooling;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "vehicles", uniqueConstraints = @UniqueConstraint(columnNames = "plateNumber"))
@Getter
@Setter
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false)
    private String ownerId;

    @OneToMany(mappedBy = "vehicle")
    private List<Trip> trips;
}
