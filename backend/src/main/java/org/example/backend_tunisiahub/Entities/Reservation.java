package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.example.backend_tunisiahub.Entities.User.User;

import org.example.backend_tunisiahub.Entities.Camping.Spot;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String status;

    @Temporal(TemporalType.DATE)
    Date startDate;

    @Temporal(TemporalType.DATE)
    Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    Date reminderSentAt;

    String reminderStatus;

    @Column(length = 500)
    String reminderError;

    Double totalPrice;

    @Enumerated(EnumType.STRING)
    ReservationType type;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    Trip trip;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    Spot spot;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @OneToMany(mappedBy = "reservation")
    List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation")
    Review review;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    Accommodation accommodation;

}
