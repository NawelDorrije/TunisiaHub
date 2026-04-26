package org.example.backend_tunisiahub.Entities.Restaurant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FloorPlanDto {

    private Long restaurantId;
    private String restaurantName;        // helpful for frontend

    private List<RestaurantTable> tables;

    // Optional metadata
    private Double canvasWidth = 900.0;   // default canvas size
    private Double canvasHeight = 600.0;
}