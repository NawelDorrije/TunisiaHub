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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
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
}
