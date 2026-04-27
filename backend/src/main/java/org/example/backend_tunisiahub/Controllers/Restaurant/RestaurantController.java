package org.example.backend_tunisiahub.Controllers.Restaurant;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.RestaurantRecommendationResponseDto;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.FloorPlanDto;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Services.RecommendationService;
import org.example.backend_tunisiahub.Services.Restaurant.IRestaurantService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final IRestaurantService restaurantService;
    private final RecommendationService recommendationService;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;

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

    @GetMapping("/recommendations")
    public RestaurantRecommendationResponseDto getRecommendations(Authentication authentication,
                                                                  HttpServletRequest request) {
        User user = resolveCurrentUser(authentication, request);
        return recommendationService.getRecommendedRestaurants(user);
    }

    @GetMapping("/get/{id}")
    public Restaurant getRestaurantById(@PathVariable Long id) {
        return restaurantService.retrieveRestaurant(id);
    }

    @GetMapping("/{id}/floor-plan")
    public FloorPlanDto getFloorPlanWithAvailability(@PathVariable Long id,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
                                                     @RequestParam int partySize) {
        return restaurantService.getFloorPlanWithAvailability(id, dateTime, partySize);
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

    private User resolveCurrentUser(Authentication authentication, HttpServletRequest request) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
            User user = userRepository.findByEmail(authentication.getName());
            if (user != null) {
                return user;
            }
        }

        String headerValue = request.getHeader(CurrentUserResolver.USER_ID_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Authentication is required. Provide a Bearer token or X-USER-ID header.");
        }

        Long userId = currentUserResolver.getUserId(request);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found for X-USER-ID header"));
    }

    private record ReverseGeocodeResponse(String address) {}
}
