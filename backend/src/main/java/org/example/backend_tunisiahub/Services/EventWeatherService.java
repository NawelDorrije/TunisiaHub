package org.example.backend_tunisiahub.Services;



import org.example.backend_tunisiahub.dto.WeatherDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class EventWeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;
    // 🌤 METEO ACTUELLE
    public WeatherDTO getWeather(double lat, double lon) {

        String url = apiUrl + "/weather?lat=" + lat +
                "&lon=" + lon +
                "&appid=" + apiKey +
                "&units=metric";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        Map<String, Object> main = (Map<String, Object>) response.get("main");
        Map<String, Object> wind = (Map<String, Object>) response.get("wind");

        WeatherDTO dto = new WeatherDTO();
        dto.setTemperature((Double) main.get("temp"));
        dto.setHumidity((Integer) main.get("humidity"));
        dto.setWindSpeed((Double) wind.get("speed"));

        return dto;
    }
    // 📅 METEO SUR PLUSIEURS JOURS
    public Map<String, Object> getWeeklyWeather(double lat, double lon) {

        String url = apiUrl + "/forecast?lat=" + lat +
                "&lon=" + lon +
                "&appid=" + apiKey +
                "&units=metric";

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, Map.class);
    }
}
