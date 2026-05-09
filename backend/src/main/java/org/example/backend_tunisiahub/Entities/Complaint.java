package org.example.backend_tunisiahub.Entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;

import java.util.Date;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String description;

    @Temporal(TemporalType.DATE)
    Date date;

    String reportedByUserId;

    String status;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    ReservationRestaurant reservation;
}
