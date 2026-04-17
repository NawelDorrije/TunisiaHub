package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.Payment;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
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

    Double totalPrice;

    @Enumerated(EnumType.STRING)
    ReservationType type;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    @JsonIgnore
    Trip trip;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    @JsonIgnore
    Spot spot;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User user;

    @OneToMany(mappedBy = "reservation")
    List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    Review review;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    Event event;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    List<Payment> payments;

}
