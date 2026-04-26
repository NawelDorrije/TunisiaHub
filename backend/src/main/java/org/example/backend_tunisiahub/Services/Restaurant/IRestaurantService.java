package org.example.backend_tunisiahub.Services.Restaurant;

import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchRequest;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.FloorPlanDto;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;

import java.time.LocalDateTime;
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

    List<Restaurant> searchAiCandidates(AiSearchRequest request, int limit);

    String resolveAddressFromCoordinates(Double latitude, Double longitude);

    FloorPlanDto getFloorPlanWithAvailability(Long restaurantId, LocalDateTime dateTime, int partySize);

}
