package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Camping.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.User.User;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate startDateCamping;

    LocalDate endDateCamping;

    int numberOfPeopleCamping;

    double totalPriceCamping;

    @Enumerated(EnumType.STRING)
    ReservationStatus statusCamping;

    @ManyToOne
    User user;

    @ManyToOne
    Spot spot;

}
