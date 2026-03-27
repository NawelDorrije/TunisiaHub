package org.example.backend_tunisiahub.Entities.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Table(name = "menus")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    @Enumerated(EnumType.STRING)
    MenuType type;
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    @JsonIgnore
    Restaurant restaurant;
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    @JsonIgnore
    List<MenuItem> items;
    @Transient
    @JsonProperty("restaurant_id")
    Long restaurantId;

    @JsonProperty("restaurant_id")
    public Long getRestaurantId() {
        if (restaurantId != null) {
            return restaurantId;
        }
        return restaurant != null ? restaurant.getId() : null;
    }


}
