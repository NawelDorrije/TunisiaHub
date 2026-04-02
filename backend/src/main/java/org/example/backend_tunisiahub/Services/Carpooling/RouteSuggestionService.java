package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RouteSuggestionService {

    private static final Logger logger = LoggerFactory.getLogger(RouteSuggestionService.class);

    @Value("${ors.api.key:}")
    private String orsApiKey;

    private static final String ORS_DIRECTIONS_URL =
            "https://api.openrouteservice.org/v2/directions/driving-car/geojson";

    public List<Map<String, Object>> getRouteSuggestions(
            double startLat,
            double startLng,
            double endLat,
            double endLng
    ) {
        if (orsApiKey == null || orsApiKey.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OpenRouteService API key is missing");
        }

        List<String> errors = new ArrayList<>();
        List<Map<String, Object>> suggestions = requestAlternativeRoutes(
                startLat,
                startLng,
                endLat,
                endLng,
                errors
        );

        if (suggestions.isEmpty()) {
            String errorMessage = errors.isEmpty()
                    ? "Unable to calculate routes"
                    : errors.get(0);
            throw new ApiException(HttpStatus.BAD_REQUEST, errorMessage);
        }

        return suggestions;
    }

    private List<Map<String, Object>> requestAlternativeRoutes(
            double startLat,
            double startLng,
            double endLat,
            double endLng,
            List<String> errors
    ) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", orsApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("coordinates", List.of(
                List.of(startLng, startLat),
                List.of(endLng, endLat)
        ));
        body.put("instructions", false);
        body.put("radiuses", List.of(10000, 10000));
        body.put("alternative_routes", Map.of(
                "target_count", 3,
                "share_factor", 0.6,
                "weight_factor", 2
        ));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    ORS_DIRECTIONS_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            return mapAlternativeRoutesResponse(response.getBody());
        } catch (HttpStatusCodeException exception) {
            String responseBody = exception.getResponseBodyAsString();
            logger.warn("ORS alternative routes request failed: status={}, body={}", exception.getStatusCode(), responseBody);
            errors.add("ORS route request failed: " + responseBody);
            return new ArrayList<>();
        } catch (Exception exception) {
            logger.warn("ORS alternative routes request failed: {}", exception.getMessage());
            errors.add("ORS route request failed: " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> mapAlternativeRoutesResponse(Map body) {
        List<Map<String, Object>> routes = new ArrayList<>();

        if (body == null || body.get("features") == null) {
            return routes;
        }

        List features = (List) body.get("features");
        if (features.isEmpty()) {
            return routes;
        }

        logger.info("ORS alternative routes count={}", features.size());

        for (int i = 0; i < features.size(); i++) {
            Map feature = (Map) features.get(i);
            if (feature == null) {
                continue;
            }

            Map properties = (Map) feature.get("properties");
            if (properties == null) {
                continue;
            }

            Map summary = (Map) properties.get("summary");
            Map geometry = (Map) feature.get("geometry");
            if (geometry == null) {
                continue;
            }

            List coordinates = (List) geometry.get("coordinates");

            if (summary == null || coordinates == null || coordinates.isEmpty()) {
                continue;
            }

            Number distanceValue = (Number) summary.get("distance");
            Number durationValue = (Number) summary.get("duration");

            if (distanceValue == null || durationValue == null) {
                logger.warn("Skipping ORS route {} because summary is incomplete: {}", i, summary);
                continue;
            }

            double distanceMeters = distanceValue.doubleValue();
            double durationSeconds = durationValue.doubleValue();
            int distanceKm = Math.max(1, (int) Math.round(distanceMeters / 1000.0));
            int durationMinutes = Math.max(1, (int) Math.round(durationSeconds / 60.0));

            Map<String, Object> route = new HashMap<>();
            route.put("label", durationMinutes + " min - " + (i == 0 ? "Recommended" : "Alternative"));
            route.put("helper", distanceKm + " km - " + (i == 0 ? "Best route" : "Route option " + (i + 1)));
            route.put("durationMinutes", durationMinutes);
            route.put("distanceKm", distanceKm);
            route.put("coordinates", coordinates);
            routes.add(route);
        }

        return routes;
    }
}
