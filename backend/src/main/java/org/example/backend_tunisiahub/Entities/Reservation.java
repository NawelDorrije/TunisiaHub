package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
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

    @Column(name = "number_of_people", nullable = true)
    Integer numberOfPeople;

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
    @JsonIgnore
    List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation")
    @JsonIgnore
    Review review;

}
