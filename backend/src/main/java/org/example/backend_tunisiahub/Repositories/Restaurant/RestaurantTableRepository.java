package org.example.backend_tunisiahub.Repositories.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {
    List<RestaurantTable> findByRestaurant_Id(Long restaurantId);

    List<RestaurantTable> findByRestaurant_IdAndStatus(Long restaurantId, TableStatus status);
}
