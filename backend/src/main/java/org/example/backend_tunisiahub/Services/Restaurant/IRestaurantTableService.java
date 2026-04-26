package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;

import java.util.List;

public interface IRestaurantTableService {

    List<RestaurantTable> retrieveAllTables();

    RestaurantTable retrieveTable(Long id);

    List<RestaurantTable> retrieveTablesByRestaurant(Long restaurantId, TableStatus status, java.time.LocalDateTime dateTime, Integer partySize);

    RestaurantTable addTable(RestaurantTable table);

    RestaurantTable modifyTable(RestaurantTable table);

    void deleteTable(Long id);

    void saveFloorPlan(Long restaurantId, List<RestaurantTable> tables);
}
