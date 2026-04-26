package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.User.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    Spot spot;
    @Builder.Default

    @ManyToMany
    @JoinTable(
            name = "reservation_activities",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    List<Activity> activities = new ArrayList<>();

    @Column(nullable = false)
    LocalDate checkIn;

    @Column(nullable = false)
    LocalDate checkOut;

    @Column( nullable = false)  // ← matches your DB column
    Integer numberOfGuests;

    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal totalPrice;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ReservationStatus status = ReservationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    String notes;

    @Builder.Default
    @Column(nullable = false)
    LocalDateTime createdAt = LocalDateTime.now();

    LocalDateTime updatedAt;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    Payment payment;
}