package org.example.backend_tunisiahub.Entities.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String address;
    @Column(unique = true)
    String email;
    @Column(unique = true)
    String phoneNum;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Menu> menus;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    @JsonIgnore
    List<RestaurantTable> tables;

    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    List<org.example.backend_tunisiahub.Entities.Reservation> reservations;

}
