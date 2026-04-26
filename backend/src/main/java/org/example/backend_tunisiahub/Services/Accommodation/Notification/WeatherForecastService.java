package org.example.backend_tunisiahub.Services.Accommodation.Notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherForecastService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<ForecastDay> getForecastForStay(Double latitude, Double longitude, LocalDate startDate, int daysCount) {
        if (latitude == null || longitude == null || startDate == null || daysCount <= 0) {
            return Collections.emptyList();
        }

        try {
            String url = "https://api.open-meteo.com/v1/forecast"
                    + "?latitude=" + latitude
                    + "&longitude=" + longitude
                    + "&daily=temperature_2m_max,temperature_2m_min,weather_code"
                    + "&forecast_days=5"
                    + "&timezone=auto";

            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isBlank()) {
                return Collections.emptyList();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode daily = root.path("daily");
            JsonNode times = daily.path("time");
            JsonNode maxTemps = daily.path("temperature_2m_max");
            JsonNode minTemps = daily.path("temperature_2m_min");
            JsonNode weatherCodes = daily.path("weather_code");

            if (!times.isArray() || !maxTemps.isArray() || !minTemps.isArray() || !weatherCodes.isArray()) {
                log.warn("Open-Meteo response missing expected daily arrays for lat={}, lon={}", latitude, longitude);
                return Collections.emptyList();
            }

            List<ForecastDay> results = new ArrayList<>();
            List<ForecastDay> allDays = new ArrayList<>();
            for (int i = 0; i < times.size() && i < maxTemps.size() && i < minTemps.size() && i < weatherCodes.size(); i++) {
                LocalDate date = LocalDate.parse(times.path(i).asText());
                ForecastDay day = new ForecastDay(
                        date,
                        minTemps.path(i).asDouble(),
                        maxTemps.path(i).asDouble(),
                        weatherCodes.path(i).asInt(),
                        weatherLabel(weatherCodes.path(i).asInt())
                );
                allDays.add(day);

                if (date.isBefore(startDate)) {
                    continue;
                }

                results.add(day);

                if (results.size() >= daysCount) {
                    break;
                }
            }

            if (!results.isEmpty()) {
                return results;
            }

            // Fallback: if start-date filtering yields no rows, still return first forecast days.
            // This keeps reminder emails useful even with timezone/date edge cases.
            if (allDays.isEmpty()) {
                return Collections.emptyList();
            }
            return allDays.stream().limit(daysCount).toList();
        } catch (Exception ex) {
            log.warn("Weather forecast lookup failed for lat={}, lon={}: {}", latitude, longitude, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String weatherLabel(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1, 2, 3 -> "Partly cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55, 56, 57 -> "Drizzle";
            case 61, 63, 65, 66, 67 -> "Rain";
            case 71, 73, 75, 77 -> "Snow";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Weather update";
        };
    }

    public record ForecastDay(
            LocalDate date,
            double minTemp,
            double maxTemp,
            int weatherCode,
            String weatherLabel
    ) {
    }
}
