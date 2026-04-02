package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.example.backend_tunisiahub.Entities.User.User;

import org.example.backend_tunisiahub.Entities.Camping.Spot;
import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    ReservationStatus status;

    @Temporal(TemporalType.DATE)
    Date startDate;

    @Temporal(TemporalType.DATE)
    Date endDate;

    Double totalPrice;

    @Enumerated(EnumType.STRING)
    ReservationType type;



    LocalDateTime dateTime;

    Integer partySize;

    @Column(length = 1000)
    String notes;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    Trip trip;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    Spot spot;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @JsonIgnoreProperties({"menus", "tables", "reservations"})
    Restaurant restaurant;

    @ManyToMany
    @JoinTable(
            name = "reservation_tables",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "table_id")
    )
    @JsonIgnoreProperties({"restaurant", "reservations"})
    List<RestaurantTable> tables;

    @OneToMany(mappedBy = "reservation")
    @JsonIgnore
    List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation")
    @JsonIgnore
    Review review;

}
