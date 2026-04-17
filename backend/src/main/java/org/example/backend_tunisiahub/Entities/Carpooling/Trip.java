package org.example.backend_tunisiahub.Entities.Carpooling;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String departurePoint;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime departureDateTime;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int seatsTotal;

    @Column(nullable = false)
    private int seatsAvailable;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private String createdBy;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "vehicle_id")
    private org.example.backend_tunisiahub.Entities.Carpooling.Vehicle vehicle;

    @OneToMany(mappedBy = "trip")
    private List<Reservation> reservations;

    @PrePersist
    void prePersist() {
        if (this.departureTime == null) {
            this.departureTime = this.departureDateTime;
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.seatsAvailable <= 0) {
            this.seatsAvailable = this.seatsTotal;
        }
        if (this.status == null) {
            this.status = "SCHEDULED";
        }
    }

    @PreUpdate
    void preUpdate() {
        this.departureTime = this.departureDateTime;
        this.updatedAt = LocalDateTime.now();
    }
}
