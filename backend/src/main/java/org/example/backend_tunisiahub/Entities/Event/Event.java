package org.example.backend_tunisiahub.Entities.Event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    @Column(length = 1000)
    String description;

    LocalDateTime startDate;

    LocalDateTime endDate;

    double price;

    int capacity;

    //String status;
    String status = "OPEN";

    //String image;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String image;

    @Enumerated(EnumType.STRING)
    EventType type;

    double latitude;
    String lieu;

    double longitude;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations = new ArrayList<>();



}