package org.example.backend_tunisiahub.Entities.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.example.backend_tunisiahub.Entities.Reservation;

import java.util.List;

@Entity
@jakarta.persistence.Table(name = "restaurant_tables")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Integer tableNumber;

    @Column(nullable = false)
    Integer capacity;

    String location;

    @Convert(converter = TableStatusConverter.class)
    @Column(nullable = false)
    TableStatus status;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonIgnoreProperties({"menus", "tables", "reservations"})
    Restaurant restaurant;
    private Double x;
    private Double y;
    private Double width;
    private Double height;
    private Double rotation;
    private String shapeType;     // "rectangle", "circle", etc.
    private String label;
    private String color;
    @ManyToMany(mappedBy = "tables")
    @JsonIgnore
    List<Reservation> reservations;

    boolean active = true;
    
    @jakarta.persistence.Transient
    private boolean isAvailable = true;
}
