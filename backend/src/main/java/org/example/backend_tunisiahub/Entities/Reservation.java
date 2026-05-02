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
    private Long id;

    // ===================== COMMON USER =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ===================== CAMPING =====================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @ManyToMany
    @JoinTable(
            name = "reservation_activities",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    @Builder.Default
    private List<Activity> activities = new ArrayList<>();

    @Column(nullable = true)
    private LocalDate checkIn;

    @Column(nullable = true)
    private LocalDate checkOut;

    @Column(nullable = true)
    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReservationStatus status;

    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ===================== ACCOMMODATION (OLD SYSTEM) =====================
    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = true)
    private Accommodation accommodation;

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = true)
    private ReservationType type;

    private Double legacyTotalPrice;

    // ===================== REMINDER SYSTEM =====================
    @Temporal(TemporalType.TIMESTAMP)
    private Date reminderSentAt;

    private String reminderStatus;

    @Column(length = 500)
    private String reminderError;

    @Column(name = "number_of_people", nullable = true)
    private Integer numberOfPeople;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "reserved_by_id")
    private User reservedBy;

    @OneToMany(mappedBy = "reservation")
    @JsonIgnore
    private List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation")
    @JsonIgnore
    private Review review;

    // ===================== AUDIT =====================
    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ===================== PAYMENT =====================
    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

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
