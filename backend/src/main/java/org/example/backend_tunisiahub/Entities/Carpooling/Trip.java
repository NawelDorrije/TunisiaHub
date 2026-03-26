package org.example.backend_tunisiahub.Entities.Carpooling;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "trips")
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String departurePoint;

    String destination;

    LocalDateTime departureDateTime;

    @JsonIgnore
    @Column(name = "departure_time", nullable = false)
    LocalDateTime departureTime;

    BigDecimal price;

    int seatsTotal;

    int seatsAvailable;

    String status;

    String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @JsonIgnore
    @Column(name = "driver_id", nullable = false)
    String driverId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    Vehicle vehicle;

    @OneToMany(mappedBy = "trip")
    @JsonIgnore
    List<Reservation> reservations;

    @PrePersist
    @PreUpdate
    void syncLegacyColumns() {
        if (departureDateTime != null) {
            departureTime = departureDateTime;
        }
        if ((driverId == null || driverId.isBlank()) && createdBy != null && !createdBy.isBlank()) {
            driverId = createdBy;
        }
        if ((createdBy == null || createdBy.isBlank()) && driverId != null && !driverId.isBlank()) {
            createdBy = driverId;
        }
    }

    @PostLoad
    void syncReadModel() {
        if ((createdBy == null || createdBy.isBlank()) && driverId != null && !driverId.isBlank()) {
            createdBy = driverId;
        }
        if (departureDateTime == null && departureTime != null) {
            departureDateTime = departureTime;
        }
    }
}
