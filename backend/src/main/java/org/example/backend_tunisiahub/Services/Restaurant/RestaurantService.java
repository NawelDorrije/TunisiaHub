package org.example.backend_tunisiahub.Services.Restaurant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchRequest;
import org.example.backend_tunisiahub.Entities.Camping.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantType;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.FloorPlanDto;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantStatus;
import org.example.backend_tunisiahub.Repositories.ReservationRestaurantRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantTableRepository;
import org.example.backend_tunisiahub.Services.MediaStorageService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.data.domain.PageRequest;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService implements IRestaurantService {

    private static final String NOMINATIM_REVERSE_URL =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&accept-language=en&lat=%s&lon=%s";
    private static final double PREFILTER_RADIUS_KM = 30.0;
    private static final Pattern CITY_PATTERN = Pattern.compile(
            "\\b(?:in|at|near|around|city|ville|a|\\u00E0|fi|f|\\u0628|\\u0641\\u064A|\\u0642\\u0631\\u0628)\\s+([\\p{L}][\\p{L}\\-']+(?:\\s+[\\p{L}][\\p{L}\\-']+)?)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );
    private static final Set<String> LOCATION_STOP_WORDS = Set.of(
            "restaurant", "restaurants", "resto", "food", "dinner", "lunch", "breakfast",
            "romantic", "family", "halal", "vegan", "vegetarian", "gluten", "free"
    );

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReservationRestaurantRepository reservationRepository;
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
    public List<Restaurant> searchAiCandidates(AiSearchRequest request, int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 30));
        String normalizedQuery = request == null || request.query() == null ? "" : request.query().trim();
        String inferredCity = extractCity(normalizedQuery).orElse("");
        Cuisine inferredCuisine = inferCuisine(normalizedQuery).orElse(null);

        List<Restaurant> baseCandidates = restaurantRepository.prefilterAiCandidates(
                normalizedQuery,
                inferredCity,
                inferredCuisine,
                PageRequest.of(0, boundedLimit * 3)
        );

        if (request != null && request.latitude().isPresent() && request.longitude().isPresent()) {
            double latitude = request.latitude().get();
            double longitude = request.longitude().get();
            validateCoordinates(latitude, longitude);

            List<Restaurant> nearbyCandidates = baseCandidates.stream()
                    .filter(restaurant -> restaurant.getLatitude() != null && restaurant.getLongitude() != null)
                    .filter(restaurant -> haversineKm(latitude, longitude, restaurant.getLatitude(), restaurant.getLongitude()) <= PREFILTER_RADIUS_KM)
                    .sorted(Comparator.comparingDouble(
                            restaurant -> haversineKm(latitude, longitude, restaurant.getLatitude(), restaurant.getLongitude())
                    ))
                    .limit(boundedLimit)
                    .toList();

            if (!nearbyCandidates.isEmpty()) {
                return nearbyCandidates;
            }
        }

        return baseCandidates.stream()
                .limit(boundedLimit)
                .collect(Collectors.toList());
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

    @Override
    public FloorPlanDto getFloorPlanWithAvailability(Long restaurantId, LocalDateTime dateTime, int partySize) {
        if (dateTime == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateTime is required");
        }
        if (partySize <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "partySize must be greater than 0");
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        List<RestaurantTable> tables = restaurantTableRepository.findByRestaurant_IdAndActiveTrue(restaurantId);

        List<ReservationRestaurant> activeReservations = reservationRepository.findByRestaurant_IdAndTypeAndDateTimeAndStatusIn(
                restaurantId,
                ReservationRestaurantType.RestaurantReservation,
                dateTime,
                EnumSet.of(ReservationRestaurantStatus.PENDING, ReservationRestaurantStatus.CONFIRMED, ReservationRestaurantStatus.ARRIVED)
        );

        Set<Long> unavailableTableIds = new HashSet<>();
        for (ReservationRestaurant reservation : activeReservations) {
            if (reservation.getTables() == null) {
                continue;
            }
            reservation.getTables().stream()
                    .map(RestaurantTable::getId)
                    .filter(java.util.Objects::nonNull)
                    .forEach(unavailableTableIds::add);
        }

        List<RestaurantTable> floorPlanTables = tables.stream()
                .map(table -> toFloorPlanTable(table, unavailableTableIds.contains(table.getId()), partySize))
                .toList();

        return new FloorPlanDto(
                restaurant.getId(),
                restaurant.getName(),
                floorPlanTables,
                900.0,
                600.0
        );
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

    private RestaurantTable toFloorPlanTable(RestaurantTable source, boolean reservedForSlot, int partySize) {
        RestaurantTable table = new RestaurantTable();
        table.setId(source.getId());
        table.setTableNumber(source.getTableNumber());
        table.setCapacity(source.getCapacity());
        table.setLocation(source.getLocation());
        table.setRestaurant(source.getRestaurant());
        table.setX(source.getX());
        table.setY(source.getY());
        table.setWidth(source.getWidth());
        table.setHeight(source.getHeight());
        table.setRotation(source.getRotation());
        table.setShapeType(source.getShapeType());
        table.setLabel(source.getLabel());
        table.setColor(source.getColor());

        if (source.getStatus() == TableStatus.OCCUPIED) {
            table.setStatus(TableStatus.OCCUPIED);
        } else if (reservedForSlot || !canSeatParty(source, partySize)) {
            table.setStatus(TableStatus.RESERVED);
        } else {
            table.setStatus(TableStatus.AVAILABLE);
        }
        return table;
    }

    private boolean canSeatParty(RestaurantTable table, int partySize) {
        return table.getCapacity() != null && table.getCapacity() >= partySize;
    }

    private Cuisine parseCuisine(String cuisine) {
        String normalized = cuisine.trim().toUpperCase();
        try {
            return Cuisine.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid cuisine '" + cuisine + "'. Allowed values: " + Arrays.toString(Cuisine.values())
            );
        }
    }

    private Optional<Cuisine> inferCuisine(String query) {
        if (!StringUtils.hasText(query)) {
            return Optional.empty();
        }

        String rawLower = query.toLowerCase(Locale.ROOT);
        String normalized = rawLower.replace('-', '_').replace(' ', '_');

        for (Cuisine cuisine : Cuisine.values()) {
            String enumName = cuisine.name().toLowerCase(Locale.ROOT);
            String humanName = enumName.replace('_', ' ');
            if (normalized.contains(enumName) || rawLower.contains(humanName)) {
                return Optional.of(cuisine);
            }
        }

        if (normalized.contains("seafood") || normalized.contains("fish")) {
            return Optional.of(Cuisine.SEAFOOD);
        }
        if (normalized.contains("italian") || normalized.contains("pizza") || normalized.contains("pasta")) {
            return Optional.of(Cuisine.ITALIAN);
        }
        if (normalized.contains("japanese") || normalized.contains("sushi")) {
            return Optional.of(Cuisine.JAPANESE);
        }
        if (normalized.contains("vegan") || normalized.contains("vegetarian")) {
            return Optional.of(Cuisine.VEGETARIAN);
        }
        if (normalized.contains("grill") || normalized.contains("bbq")) {
            return Optional.of(Cuisine.GRILL);
        }
        if (normalized.contains("street_food") || normalized.contains("street food") || normalized.contains("sandwich")) {
            return Optional.of(Cuisine.STREET_FOOD);
        }
        if (normalized.contains("mediterranean")) {
            return Optional.of(Cuisine.MEDITERRANEAN);
        }
        if (normalized.contains("traditional") || normalized.contains("tunisian")) {
            return Optional.of(Cuisine.TRADITIONAL);
        }
        return Optional.empty();
    }

    private Optional<String> extractCity(String query) {
        if (!StringUtils.hasText(query)) {
            return Optional.empty();
        }

        Matcher matcher = CITY_PATTERN.matcher(query);
        while (matcher.find()) {
            String city = matcher.group(1);
            if (StringUtils.hasText(city) && !LOCATION_STOP_WORDS.contains(city.toLowerCase(Locale.ROOT))) {
                return Optional.of(city.trim());
            }
        }
        return Optional.empty();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }
}
