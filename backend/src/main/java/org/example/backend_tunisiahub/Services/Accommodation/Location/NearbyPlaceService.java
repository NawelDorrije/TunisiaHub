package org.example.backend_tunisiahub.Services.Accommodation.Location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearbyPlaceService {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";
    private static final int SEARCH_RADIUS_METERS = 10000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public PlaceInfo findNearestHospital(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return PlaceInfo.notAvailable();
        }

        String query = "[out:json][timeout:12];(node[\"amenity\"=\"hospital\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");way[\"amenity\"=\"hospital\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");relation[\"amenity\"=\"hospital\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + "););out center;";
        return queryNearestPlace(query, latitude, longitude);
    }

    public PlaceInfo findNearestMarket(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return PlaceInfo.notAvailable();
        }

        String query = "[out:json][timeout:12];(node[\"shop\"=\"supermarket\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");way[\"shop\"=\"supermarket\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");relation[\"shop\"=\"supermarket\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");node[\"amenity\"=\"marketplace\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");way[\"amenity\"=\"marketplace\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + ");relation[\"amenity\"=\"marketplace\"](around:" + SEARCH_RADIUS_METERS + "," + latitude + "," + longitude + "););out center;";
        return queryNearestPlace(query, latitude, longitude);
    }

    private PlaceInfo queryNearestPlace(String overpassQuery, Double latitude, Double longitude) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            String response = restTemplate.postForObject(
                    OVERPASS_URL,
                    new HttpEntity<>(overpassQuery, headers),
                    String.class
            );

            if (response == null || response.isBlank()) {
                return PlaceInfo.notAvailable();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode elements = root.path("elements");
            if (!elements.isArray() || elements.isEmpty()) {
                return PlaceInfo.notAvailable();
            }

            Optional<PlaceInfo> nearest = Optional.empty();
            for (JsonNode element : elements) {
                JsonNode tags = element.path("tags");
                String name = tags.path("name").asText("").trim();
                if (name.isEmpty()) {
                    name = "Unnamed place";
                }

                double placeLat;
                double placeLon;

                if (element.has("lat") && element.has("lon")) {
                    placeLat = element.path("lat").asDouble();
                    placeLon = element.path("lon").asDouble();
                } else if (element.has("center")) {
                    placeLat = element.path("center").path("lat").asDouble();
                    placeLon = element.path("center").path("lon").asDouble();
                } else {
                    continue;
                }

                double distanceKm = haversine(latitude, longitude, placeLat, placeLon);
                PlaceInfo candidate = new PlaceInfo(name, distanceKm, placeLat, placeLon);

                nearest = nearest
                        .map(existing -> Comparator.comparingDouble(PlaceInfo::distanceKm).compare(candidate, existing) < 0 ? candidate : existing)
                        .or(() -> Optional.of(candidate));
            }

            return nearest.orElseGet(PlaceInfo::notAvailable);
        } catch (Exception ex) {
            log.warn("Failed to fetch nearby places from Overpass API: {}", ex.getMessage());
            return PlaceInfo.notAvailable();
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    public record PlaceInfo(String name, double distanceKm, Double latitude, Double longitude) {
        public static PlaceInfo notAvailable() {
            return new PlaceInfo("Not available", -1, null, null);
        }

        public String displayDistance() {
            if (distanceKm < 0) {
                return "N/A";
            }
            return String.format("%.1f km", distanceKm);
        }

        public boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }

        public String coordinates() {
            if (!hasCoordinates()) {
                return "N/A";
            }
            return String.format("%.6f, %.6f", latitude, longitude);
        }

        public String openStreetMapUrl() {
            if (!hasCoordinates()) {
                return "";
            }
            return String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=16/%.6f/%.6f",
                    latitude, longitude, latitude, longitude);
        }

        public String googleMapsUrl() {
            if (!hasCoordinates()) {
                return "";
            }
            return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
        }
    }
}
