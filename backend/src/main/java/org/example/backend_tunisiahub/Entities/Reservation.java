package org.example.backend_tunisiahub.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.User.User;

import org.example.backend_tunisiahub.Entities.Camping.Spot;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(nullable = true)
    private Date endDate;

    private Double totalPrice;

    @Column(name = "number_of_people", nullable = true)
    private Integer numberOfPeople;

    @Enumerated(EnumType.STRING)
    private ReservationType type;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reservedBy;

    @OneToMany(mappedBy = "reservation")
    @JsonIgnore
    private List<Complaint> complaints;

    @OneToOne(mappedBy = "reservation")
    @JsonIgnore
    private Review review;

}
