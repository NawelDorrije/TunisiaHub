package org.example.backend_tunisiahub.Entities.User;

import com.fasterxml.jackson.annotation.JsonIgnore;
<<<<<<< HEAD
=======

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

>>>>>>> origin/feature/integrated-app-event
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
<<<<<<< HEAD
=======

import org.example.backend_tunisiahub.Entities.Event.Event;
//import org.example.backend_tunisiahub.Entities.Event.Rating;
>>>>>>> origin/feature/integrated-app-event
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

<<<<<<< HEAD
=======

import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

import java.util.ArrayList;
import java.util.List;



>>>>>>> origin/feature/integrated-app-event
@Entity
@Table(name = "users")
@Getter
@Setter
<<<<<<< HEAD
=======


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

>>>>>>> origin/feature/integrated-app-event
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nom;

    String prenom;

    @Column(unique = true)
    String email;

<<<<<<< HEAD
    @JsonIgnore
    String motDePasse;
    @Enumerated(EnumType.STRING)
    RoleUser role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations;
}
=======
  @JsonIgnore
    String motDePasse;
    @Enumerated(EnumType.STRING)
    RoleUser role;

    //@JsonIgnore
    //String motDePasse;

    //@Enumerated(EnumType.STRING)
    //RoleUser role;

    // ================= RESERVATIONS =================

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Reservation> reservations;


    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Event> createdEvents;

    //@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    //List<Rating> ratings;


    // ================= ORDERS =================
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    List<Order> orders;

    // ================= REVIEWS =================
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    List<Review> reviews;

    // ================= CAMPINGS (OWNER) =================
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Camping> campings = new ArrayList<>();
}


>>>>>>> origin/feature/integrated-app-event
