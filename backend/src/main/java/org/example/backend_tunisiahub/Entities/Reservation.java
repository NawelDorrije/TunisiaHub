package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.User.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    // ===================== COMMON USER =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    // ===================== CAMPING =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    Spot spot;

    @ManyToMany
    @JoinTable(
            name = "reservation_activities",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    @Builder.Default
    List<Activity> activities = new ArrayList<>();

    @Column(nullable = true)
    LocalDate checkIn;

    @Column(nullable = true)
    LocalDate checkOut;

    @Column(nullable = true)
    Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    ReservationStatus status;

    @Column(nullable = true, precision = 10, scale = 2)
    BigDecimal totalPrice;

    @Column(columnDefinition = "TEXT")
    String notes;

    // ===================== ACCOMMODATION (OLD SYSTEM) =====================
    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = true)
    Accommodation accommodation;

    @Temporal(TemporalType.DATE)
    Date startDate;

    @Temporal(TemporalType.DATE)
    Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = true)
    ReservationType type;

    Double legacyTotalPrice;

    

    // ===================== REMINDER SYSTEM =====================
    @Temporal(TemporalType.TIMESTAMP)
    Date reminderSentAt;

    String reminderStatus;

    @Column(length = 500)
    String reminderError;

    // ===================== AUDIT =====================
    @Builder.Default
    @Column(updatable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime updatedAt;

    // ===================== PAYMENT =====================
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    Payment payment;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (type == null) {
            if (spot != null) {
                type = ReservationType.CampingReservation;
            } else if (accommodation != null) {
                type = ReservationType.accommodationReservation;
            } else if (trip != null) {
                type = ReservationType.TripReservation;
            }
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
