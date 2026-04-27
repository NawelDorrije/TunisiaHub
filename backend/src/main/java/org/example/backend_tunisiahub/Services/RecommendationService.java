package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.RestaurantRecommendationItemDto;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.RestaurantRecommendationResponseDto;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private static final double MIN_RATING = 4.0;
    private static final double FALLBACK_MIN_RATING = 0.0;
    private static final int MAX_RESULTS = 10;
    private static final int RECENT_EXCLUSION_DAYS = 30;

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final AiService aiService;

    public RestaurantRecommendationResponseDto getRecommendedRestaurants(User user) {
        if (user == null || user.getId() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }

        try {
            log.info("[Recommendations] Starting for userId={}", user.getId());

            List<Reservation> history = reservationRepository.findRecommendationHistoryByUser(
                    user.getId(),
                    ReservationType.RestaurantReservation,
                    ReservationStatus.CANCELLED,
                    LocalDateTime.now()
            ).stream().filter(Objects::nonNull).toList();

            log.info("[Recommendations] Reservation history count={}", history.size());

            if (history.isEmpty()) {
                List<Restaurant> topRated = validatePersistedRestaurants(fetchTopRatedRestaurants(Set.of()));
                log.info("[Recommendations] No history, returning top-rated count={}", topRated.size());
                return new RestaurantRecommendationResponseDto(
                        aiService.generateRecommendationReason(null, null, null, false, "Popular restaurants near you"),
                        mapRestaurants(topRated)
                );
            }

            UserPreferenceProfile profile = buildProfile(history);
            log.info("[Recommendations] Profile: cuisine={}, priceRange={}, time={}",
                    profile.favoriteCuisine(), profile.preferredPriceRange(), profile.preferredTime());

            Set<Long> recentlyReservedRestaurantIds = reservationRepository.findRecentRestaurantReservationsByUser(
                            user.getId(),
                            ReservationType.RestaurantReservation,
                            ReservationStatus.CANCELLED,
                            LocalDateTime.now().minusDays(RECENT_EXCLUSION_DAYS)
                    ).stream()
                    .filter(Objects::nonNull)
                    .map(Reservation::getRestaurant)
                    .filter(Objects::nonNull)
                    .map(Restaurant::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            log.info("[Recommendations] Recently reserved IDs (excluded): {}", recentlyReservedRestaurantIds);

            List<Restaurant> recommendations = fetchPersonalizedRestaurants(profile, recentlyReservedRestaurantIds);
            log.info("[Recommendations] Pass 1 personalized+exclusions count={}", recommendations.size());

            if (recommendations.isEmpty()) {
                recommendations = fetchTopRatedRestaurants(recentlyReservedRestaurantIds);
                log.info("[Recommendations] Pass 2 top-rated+exclusions count={}", recommendations.size());
            }

            if (recommendations.isEmpty()) {
                recommendations = fetchPersonalizedRestaurants(profile, Set.of());
                log.info("[Recommendations] Pass 3 personalized without exclusions count={}", recommendations.size());
            }

            if (recommendations.isEmpty()) {
                recommendations = fetchTopRatedRestaurants(Set.of());
                log.info("[Recommendations] Pass 4 top-rated without exclusions count={}", recommendations.size());
            }

            recommendations = validatePersistedRestaurants(recommendations);
            log.info("[Recommendations] Final after validation count={}, ids={}",
                    recommendations.size(),
                    recommendations.stream().filter(Objects::nonNull).map(Restaurant::getId).toList());

            String fallbackReason = buildFallbackReason(profile, recommendations.isEmpty());
            return new RestaurantRecommendationResponseDto(
                    aiService.generateRecommendationReason(
                            profile.favoriteCuisine(),
                            profile.preferredPriceRange(),
                            profile.preferredTime(),
                            true,
                            fallbackReason
                    ),
                    mapRestaurants(recommendations)
            );
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("[Recommendations] Unexpected failure for userId={}. Falling back to top-rated DB restaurants.",
                    user.getId(), ex);
            List<Restaurant> topRated = validatePersistedRestaurants(fetchTopRatedRestaurants(Set.of()));
            return new RestaurantRecommendationResponseDto(
                    aiService.generateRecommendationReason(null, null, null, false, "Popular restaurants near you"),
                    mapRestaurants(topRated)
            );
        }
    }

    private List<Restaurant> fetchPersonalizedRestaurants(UserPreferenceProfile profile, Set<Long> excludedIds) {
        List<Restaurant> recommendations = findFromDatabase(profile.favoriteCuisine(), profile.preferredPriceRange(), excludedIds);
        if (!recommendations.isEmpty()) {
            return recommendations;
        }

        recommendations = findFromDatabase(profile.favoriteCuisine(), null, excludedIds);
        if (!recommendations.isEmpty()) {
            return recommendations;
        }

        return findFromDatabase(null, profile.preferredPriceRange(), excludedIds);
    }

    private List<Restaurant> findFromDatabase(Cuisine cuisine, String priceRange, Set<Long> excludedIds) {
        String normalizedPriceRange = normalizePriceRange(priceRange);

        // Two-pass: try HIGH rating threshold first, then fall back to 0 to include unrated restaurants
        for (double minRating : new double[]{MIN_RATING, FALLBACK_MIN_RATING}) {
            List<Restaurant> raw;

            if (cuisine != null && normalizedPriceRange != null) {
                raw = restaurantRepository.findByCuisineAndPriceRangeAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
                        cuisine, normalizedPriceRange, minRating);
            } else if (cuisine != null) {
                raw = restaurantRepository.findByCuisineAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
                        cuisine, minRating);
            } else if (normalizedPriceRange != null) {
                raw = restaurantRepository.findByPriceRangeAndRatingGreaterThanEqualOrderByRatingDescIdDesc(
                        normalizedPriceRange, minRating);
            } else {
                raw = restaurantRepository.findByRatingGreaterThanEqualOrderByRatingDescIdDesc(minRating);
            }

            List<Restaurant> filtered = raw.stream()
                    .filter(Objects::nonNull)
                    .filter(r -> r.getId() != null)
                    .filter(r -> excludedIds == null || excludedIds.isEmpty() || !excludedIds.contains(r.getId()))
                    .limit(MAX_RESULTS)
                    .toList();

            log.info("[Recommendations] findFromDatabase cuisine={} priceRange={} minRating={} rawCount={} filteredCount={}",
                    cuisine, normalizedPriceRange, minRating, raw.size(), filtered.size());

            if (!filtered.isEmpty()) {
                return filtered;
            }
        }
        return List.of();
    }

    private List<Restaurant> fetchTopRatedRestaurants(Set<Long> excludedIds) {
        PageRequest pageRequest = PageRequest.of(0, MAX_RESULTS);
        // Two-pass rating fallback
        for (double minRating : new double[]{MIN_RATING, FALLBACK_MIN_RATING}) {
            List<Restaurant> results;
            if (excludedIds == null || excludedIds.isEmpty()) {
                results = restaurantRepository.findTopRatedRestaurants(minRating, pageRequest);
            } else {
                results = restaurantRepository.findTopRatedRestaurantsExcluding(minRating, excludedIds, pageRequest);
            }
            log.info("[Recommendations] fetchTopRatedRestaurants minRating={} excludedCount={} resultCount={}",
                    minRating, excludedIds == null ? 0 : excludedIds.size(), results.size());
            if (!results.isEmpty()) {
                return results;
            }
        }
        return List.of();
    }

    private UserPreferenceProfile buildProfile(List<Reservation> history) {
        Cuisine favoriteCuisine = mostFrequentCuisine(history);
        String preferredPriceRange = mostFrequentPriceRange(history);
        String preferredTime = mostFrequentTime(history);
        return new UserPreferenceProfile(favoriteCuisine, preferredPriceRange, preferredTime);
    }

    private Cuisine mostFrequentCuisine(List<Reservation> history) {
        return history.stream()
                .map(Reservation::getRestaurant)
                .filter(Objects::nonNull)
                .map(Restaurant::getCuisine)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), () -> new EnumMap<>(Cuisine.class), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.<Cuisine, Long>comparingByValue()
                        .thenComparing(entry -> entry.getKey().name()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String mostFrequentPriceRange(List<Reservation> history) {
        Map<String, Long> counts = new HashMap<>();
        for (Reservation reservation : history) {
            if (reservation == null) {
                continue;
            }
            String priceRange = derivePriceRange(reservation.getTotalPrice());
            if (priceRange != null) {
                counts.merge(priceRange, 1L, Long::sum);
            }
        }

        return counts.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String mostFrequentTime(List<Reservation> history) {
        Map<String, Long> counts = new HashMap<>();
        for (Reservation reservation : history) {
            if (reservation == null || reservation.getDateTime() == null) {
                continue;
            }
            String slot = reservation.getDateTime().getHour() < 16 ? "LUNCH" : "DINNER";
            counts.merge(slot, 1L, Long::sum);
        }

        return counts.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String derivePriceRange(Double totalPrice) {
        if (totalPrice == null) {
            return null;
        }
        if (totalPrice < 40) {
            return "BUDGET";
        }
        if (totalPrice < 100) {
            return "MID_RANGE";
        }
        return "PREMIUM";
    }

    private String buildFallbackReason(UserPreferenceProfile profile, boolean noResults) {
        if (profile.favoriteCuisine() == null) {
            return noResults ? "Popular restaurants near you" : "Recommended based on your recent dining preferences";
        }

        String cuisineLabel = formatCuisine(profile.favoriteCuisine());
        if (StringUtils.hasText(profile.preferredTime())) {
            return "Because you liked " + cuisineLabel + " restaurants for " + profile.preferredTime().toLowerCase(Locale.ROOT);
        }
        return "Because you liked " + cuisineLabel + " restaurants";
    }

    private List<RestaurantRecommendationItemDto> mapRestaurants(List<Restaurant> restaurants) {
        return restaurants.stream()
                .filter(Objects::nonNull)
                .map(restaurant -> new RestaurantRecommendationItemDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getCuisine() == null ? null : formatCuisine(restaurant.getCuisine()),
                        restaurant.getRating(),
                        restaurant.getPicture()
                ))
                .toList();
    }

    private List<Restaurant> validatePersistedRestaurants(List<Restaurant> restaurants) {
        if (restaurants == null || restaurants.isEmpty()) {
            log.debug("Recommendation verification: no restaurant IDs returned from database query.");
            return List.of();
        }

        List<Long> idsInOrder = restaurants.stream()
                .filter(Objects::nonNull)
                .map(Restaurant::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        log.debug("Recommendation verification: repository returned restaurant IDs={}", idsInOrder);

        if (idsInOrder.isEmpty()) {
            log.debug("Recommendation verification: returned restaurants had no valid IDs.");
            return List.of();
        }

        Map<Long, Restaurant> persistedById = restaurantRepository.findAllById(idsInOrder).stream()
                .filter(Objects::nonNull)
                .filter(restaurant -> restaurant.getId() != null)
                .collect(Collectors.toMap(
                        Restaurant::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        log.debug("Recommendation verification: existing DB restaurant IDs={}", persistedById.keySet());

        List<Restaurant> verifiedRestaurants = idsInOrder.stream()
                .map(persistedById::get)
                .filter(Objects::nonNull)
                .limit(MAX_RESULTS)
                .toList();

        log.debug("Recommendation verification: verified recommendation IDs={}",
                verifiedRestaurants.stream().map(Restaurant::getId).toList());

        return verifiedRestaurants;
    }

    private String formatCuisine(Cuisine cuisine) {
        String lower = cuisine.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private String normalizePriceRange(String priceRange) {
        if (!StringUtils.hasText(priceRange)) {
            return null;
        }
        return priceRange.trim().toUpperCase(Locale.ROOT);
    }

    private record UserPreferenceProfile(
            Cuisine favoriteCuisine,
            String preferredPriceRange,
            String preferredTime
    ) {
    }
}
