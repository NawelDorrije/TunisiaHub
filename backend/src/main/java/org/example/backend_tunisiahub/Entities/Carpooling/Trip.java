<<<<<<< HEAD
package org.example.backend_tunisiahub.carpooling.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.backend_tunisiahub.Entities.Reservation;
=======
package org.example.backend_tunisiahub.Entities.Carpooling;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
>>>>>>> origin/feature/integrated-app-event

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
<<<<<<< HEAD
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
    private Vehicle vehicle;

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
=======
@Getter
@Setter
@Table(name = "trips")
@NoArgsConstructor
public class Trip {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "departure")
  private String departure;

  @Column(name = "departure_point")
  private String departurePoint;

  private String destination;

  private LocalDateTime departureDateTime;
  @Column(name = "departure_time")
  private String departureTime;

  private Integer durationMinutes;

  private BigDecimal price;

  private int seatsTotal;
  @Column(name = "seats_available")
  private Integer seatsAvailable;

  private String status;

  @Column(name = "booking_mode")
  private String bookingMode;

  @ManyToOne
  @JoinColumn(name = "driver_id", nullable = false)
  private User driver;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "created_by")
  private String createdBy;

  @JsonIgnore
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "trip")
  @JsonIgnore
  private List<Reservation> reservations;
>>>>>>> origin/feature/integrated-app-event
}
