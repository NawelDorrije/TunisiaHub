package org.example.backend_tunisiahub.Entities.Restaurant;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    String picture;
    String ingredients;
    String description;
    @Column(nullable = false, precision = 10, scale = 2)
    BigDecimal price;
    @ManyToOne
    @JsonIgnore
    Menu menu;
    @Transient
    @JsonProperty("menu_id")
    Long menuId;

    @JsonProperty("menu_id")
    public Long getMenuId() {
        if (menuId != null) {
            return menuId;
        }
        return menu != null ? menu.getId() : null;
    }
}
