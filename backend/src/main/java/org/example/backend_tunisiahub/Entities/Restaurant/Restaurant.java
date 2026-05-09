package org.example.backend_tunisiahub.Entities.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
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
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String picture;

    String address;
    Double latitude;
    Double longitude;
    @Convert(converter = CuisineConverter.class)
    Cuisine cuisine;
    String priceRange;
    Double rating;
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
    private Double x;                  // position on canvas
    private Double y;
    private Double width;              // size of the shape
    private Double height;
    private Double rotation;           // degrees, for rotated tables
    private String shapeType;
    private String label;              // displayed on the table (optional)
    private String color;
    @OneToMany(mappedBy = "restaurant")
    @JsonIgnore
    List<ReservationRestaurant> reservations;

}
