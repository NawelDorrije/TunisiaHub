package org.example.backend_tunisiahub.Controllers.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Services.Restaurant.IRestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final IRestaurantService restaurantService;

    @GetMapping
    public List<Restaurant> getAllRestaurants() {
        return restaurantService.retrieveAllRestaurants();
    }

    @GetMapping("/cuisines")
    public List<Cuisine> getCuisines() {
        return restaurantService.retrieveCuisines();
    }

    @GetMapping("/cuisines/used")
    public List<Cuisine> getUsedCuisines() {
        return restaurantService.retrieveUsedCuisines();
    }

    @GetMapping("/by-cuisine/{cuisine}")
    public List<Restaurant> getRestaurantsByCuisine(@PathVariable String cuisine) {
        return restaurantService.retrieveRestaurantsByCuisine(cuisine);
    }

    @GetMapping("/reverse-geocode")
    public ReverseGeocodeResponse reverseGeocode(@RequestParam("lat") Double latitude,
                                                 @RequestParam("lng") Double longitude) {
        String address = restaurantService.resolveAddressFromCoordinates(latitude, longitude);
        return new ReverseGeocodeResponse(address);
    }

    @GetMapping("/get/{id}")
    public Restaurant getRestaurantById(@PathVariable Long id) {
        return restaurantService.retrieveRestaurant(id);
    }

    @PostMapping("/add")
    public Restaurant createRestaurant(@RequestBody Restaurant restaurant) {
        return restaurantService.addRestaurant(restaurant);
    }

    @PutMapping("/update")
    public Restaurant updateRestaurant(@RequestBody Restaurant restaurant) {
        return restaurantService.modifyRestaurant(restaurant);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
    }

    private record ReverseGeocodeResponse(String address) {}
}
