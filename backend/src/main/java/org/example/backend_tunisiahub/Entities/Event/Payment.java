package org.example.backend_tunisiahub.Entities.Event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Event.PaymentMethod;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    double amount;

    LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    PaymentMethod method;

    @OneToOne
    @JoinColumn(name = "reservation_id")
    Reservation reservation;

    //public void setStatus(String success) {
    //}
}
