package org.example.backend_tunisiahub.Services.Camping.Pricing;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Fetches 7-day weather forecast from Open-Meteo (100% free, no API key).
 *
 * weatherScore meaning:
 *   1.0 → perfect sunny camping weather
 *   0.0 → severe storm / dangerous conditions
 *
 * Deduction rules (per forecast day):
 *   -0.15 per rainy day  (precipitation > 3 mm)
 *   -0.10 per stormy day (wind > 50 km/h)
 *   -0.05 per very hot day (max temp > 40 °C)
 *
 * URL: https://open-meteo.com/en/docs
 */
@Service
public class WeatherService {

    private static final String URL_TEMPLATE =
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=%s&longitude=%s" +
                    "&daily=precipitation_sum,windspeed_10m_max,temperature_2m_max" +
                    "&forecast_days=7&timezone=auto";

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public double fetchWeatherScore(BigDecimal latitude, BigDecimal longitude) {
        try {
            String url = String.format(URL_TEMPLATE, latitude, longitude);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("daily")) {
                return 0.70; // neutral fallback
            }

            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            List<Double> precip = (List<Double>) daily.get("precipitation_sum");
            List<Double> wind   = (List<Double>) daily.get("windspeed_10m_max");
            List<Double> temp   = (List<Double>) daily.get("temperature_2m_max");

            double score = 1.0;
            int days = precip != null ? precip.size() : 0;

            for (int i = 0; i < days; i++) {
                double p = safeGet(precip, i);
                double w = safeGet(wind, i);
                double t = safeGet(temp, i);

                if (p > 3)  score -= 0.15;
                if (w > 50) score -= 0.10;
                if (t > 40) score -= 0.05;
            }

            return Math.max(0.0, Math.min(1.0, score));

        } catch (Exception e) {
            System.err.println("[WeatherService] Fallback to 0.70 — " + e.getMessage());
            return 0.70;
        }
    }

    private double safeGet(List<Double> list, int i) {
        if (list == null || i >= list.size() || list.get(i) == null) return 0.0;
        return list.get(i);
    }
}
