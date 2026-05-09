package org.example.backend_tunisiahub.Entities;

<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonIgnore;


>>>>>>> origin/feature/integrated-app-event
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

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

<<<<<<< HEAD
    @ManyToOne
    @JoinColumn(name = "reservation_id")
=======
    @Column(length = 1000)
    String aiSummary;

    @Column(length = 1000)
    String aiKeywords;

    @Column(length = 2000)
    String aiSolutions;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
>>>>>>> origin/feature/integrated-app-event
    Reservation reservation;
}
