package org.example.backend_tunisiahub.Entities.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.example.backend_tunisiahub.Entities.Event.Event;
//import org.example.backend_tunisiahub.Entities.Event.Rating;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;


import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Order;
import org.example.backend_tunisiahub.Entities.SouvenirsShops.Review;

import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "users")
@Getter
@Setter


@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

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


