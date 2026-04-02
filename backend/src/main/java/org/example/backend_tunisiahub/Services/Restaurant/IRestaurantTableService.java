package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;

import java.util.List;

public interface IRestaurantTableService {

    List<RestaurantTable> retrieveAllTables();

    RestaurantTable retrieveTable(Long id);

    List<RestaurantTable> retrieveTablesByRestaurant(Long restaurantId, TableStatus status);

    RestaurantTable addTable(RestaurantTable table);

    RestaurantTable modifyTable(RestaurantTable table);

    void deleteTable(Long id);
}
