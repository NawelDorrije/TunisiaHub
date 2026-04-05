package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.Services.MediaStorageService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RestaurantService implements IRestaurantService {

    private static final String NOMINATIM_REVERSE_URL =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=en&lat=%s&lon=%s";

    private final RestaurantRepository restaurantRepository;
    private final MediaStorageService mediaStorageService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<Restaurant> retrieveAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Override
    public Restaurant retrieveRestaurant(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    @Override
    public Restaurant addRestaurant(Restaurant restaurant) {
        normalizePicture(restaurant);
        enrichAddressFromCoordinatesIfNeeded(restaurant);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Override
    public Restaurant modifyRestaurant(Restaurant restaurant) {
        normalizePicture(restaurant);
        enrichAddressFromCoordinatesIfNeeded(restaurant);
        return restaurantRepository.save(restaurant);
    }

    @Override
    public List<Cuisine> retrieveCuisines() {
        return Arrays.asList(Cuisine.values());
    }

    @Override
    public List<Cuisine> retrieveUsedCuisines() {
        return restaurantRepository.findDistinctCuisines();
    }

    @Override
    public List<Restaurant> retrieveRestaurantsByCuisine(String cuisine) {
        if (!StringUtils.hasText(cuisine)) {
            return restaurantRepository.findAll();
        }
        Cuisine parsedCuisine = parseCuisine(cuisine);
        return restaurantRepository.findByCuisine(parsedCuisine);
    }

    @Override
    public String resolveAddressFromCoordinates(Double latitude, Double longitude) {
        validateCoordinates(latitude, longitude);

        String lat = URLEncoder.encode(String.format(Locale.US, "%.7f", latitude), StandardCharsets.UTF_8);
        String lon = URLEncoder.encode(String.format(Locale.US, "%.7f", longitude), StandardCharsets.UTF_8);
        String url = String.format(NOMINATIM_REVERSE_URL, lat, lon);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("User-Agent", "TunisiaHubBackend/1.0")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to resolve address from map coordinates");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String displayName = root.path("display_name").asText(null);
            if (!StringUtils.hasText(displayName)) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "No address found for the selected map location");
            }
            return displayName;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Address lookup was interrupted");
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Address lookup failed");
        }
    }

    private void enrichAddressFromCoordinatesIfNeeded(Restaurant restaurant) {
        if (restaurant == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Restaurant payload is required");
        }

        boolean hasLatitude = restaurant.getLatitude() != null;
        boolean hasLongitude = restaurant.getLongitude() != null;
        if (hasLatitude ^ hasLongitude) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Both latitude and longitude are required together");
        }

        if (hasLatitude && hasLongitude) {
            validateCoordinates(restaurant.getLatitude(), restaurant.getLongitude());
            if (!StringUtils.hasText(restaurant.getAddress())) {
                String resolvedAddress = resolveAddressFromCoordinates(restaurant.getLatitude(), restaurant.getLongitude());
                restaurant.setAddress(resolvedAddress);
            }
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Longitude must be between -180 and 180");
        }
    }

    private void normalizePicture(Restaurant restaurant) {
        if (restaurant == null || !StringUtils.hasText(restaurant.getPicture())) {
            return;
        }

        String picture = restaurant.getPicture().trim();
        if (picture.startsWith("data:")) {
            restaurant.setPicture(mediaStorageService.storeRestaurantPicture(picture));
            return;
        }
        if (picture.length() > 2048) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Picture path is too long");
        }
    }

    private Cuisine parseCuisine(String cuisine) {
        String normalized = cuisine.trim().toUpperCase();
        try {
            return Cuisine.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid cuisine '" + cuisine + "'. Allowed values: " + java.util.Arrays.toString(Cuisine.values())
            );
        }
    }
}
