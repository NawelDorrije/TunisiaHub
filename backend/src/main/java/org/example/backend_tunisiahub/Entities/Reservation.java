package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.Payment;
import org.example.backend_tunisiahub.Entities.User.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  // ================= USER =================
  @ManyToOne
  @JoinColumn(name = "user_id")
  User user;

  @ManyToOne
  @JoinColumn(name = "reserved_by_id")
  User reservedBy;

  // ================= STATUS =================
  @Enumerated(EnumType.STRING)
  ReservationStatus status;

  // ================= TYPE =================
  @Enumerated(EnumType.STRING)
  ReservationType type;

  // ================= TRIP =================
  @ManyToOne
  @JoinColumn(name = "trip_id")
  Trip trip;

  Integer numberOfPeople;

  // ================= CAMPING =================
  @ManyToOne
  @JoinColumn(name = "spot_id")
  Spot spot;

  @ManyToMany
  @JoinTable(
    name = "reservation_activities",
    joinColumns = @JoinColumn(name = "reservation_id"),
    inverseJoinColumns = @JoinColumn(name = "activity_id")
  )
  List<Activity> activities = new ArrayList<>();

  LocalDate checkIn;
  LocalDate checkOut;
  Integer numberOfGuests;

  // ================= EVENT =================
  @ManyToOne
  @JoinColumn(name = "event_id")
  Event event;

  // ================= ACCOMMODATION =================
  @ManyToOne
  @JoinColumn(name = "accommodation_id")
  Accommodation accommodation;

  @Temporal(TemporalType.DATE)
  Date startDate;

  @Temporal(TemporalType.DATE)
  Date endDate;

  // ================= PAYMENT =================
  @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
  Payment payment;

  // ================= COMMON =================
  BigDecimal totalPrice;

  String notes;

  // ================= REMINDER =================
  @Temporal(TemporalType.TIMESTAMP)
  Date reminderSentAt;

  @Column
  String reminderStatus;

  @Column
  String reminderError;

  // ================= AUDIT =================
  @Column(updatable = false)
  LocalDateTime createdAt = LocalDateTime.now();

  LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    updatedAt = LocalDateTime.now();

    if (type == null) {
      if (trip != null) type = ReservationType.TripReservation;
      else if (event != null) type = ReservationType.EventReservation;
      else if (spot != null) type = ReservationType.CampingReservation;
      else if (accommodation != null) type = ReservationType.accommodationReservation;
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
