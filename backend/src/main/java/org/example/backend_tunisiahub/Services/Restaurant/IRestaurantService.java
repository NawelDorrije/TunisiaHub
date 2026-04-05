package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;

import java.util.List;

public interface IRestaurantService {

    List<Restaurant> retrieveAllRestaurants();

    Restaurant retrieveRestaurant(Long id);

    Restaurant addRestaurant(Restaurant restaurant);

    void deleteRestaurant(Long id);

    Restaurant modifyRestaurant(Restaurant restaurant);

    List<Cuisine> retrieveCuisines();

    List<Cuisine> retrieveUsedCuisines();

    List<Restaurant> retrieveRestaurantsByCuisine(String cuisine);

    String resolveAddressFromCoordinates(Double latitude, Double longitude);

}
