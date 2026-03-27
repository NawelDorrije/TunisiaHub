package org.example.backend_tunisiahub.Repositories.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
