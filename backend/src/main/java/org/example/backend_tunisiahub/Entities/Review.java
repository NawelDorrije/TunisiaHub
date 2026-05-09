package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;
<<<<<<< HEAD

@Entity
=======
@Entity(name = "ReservationReview")
@Table(name = "reservation_reviews")

>>>>>>> origin/feature/integrated-app-event
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor

public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String comment;

    Integer rating;

    @Temporal(TemporalType.DATE)
    Date date;

    @OneToOne
    @JoinColumn(name = "reservation_id")
<<<<<<< HEAD
=======
    @JsonIgnore
>>>>>>> origin/feature/integrated-app-event
    Reservation reservation;
}
