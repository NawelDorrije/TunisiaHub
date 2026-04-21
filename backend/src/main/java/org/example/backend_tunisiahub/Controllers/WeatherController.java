package org.example.backend_tunisiahub.Controllers;



import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Services.WeatherService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/weather")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public Object getWeather(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return weatherService.getWeather(lat, lon);
    }
    // 📅 WEEKLY FORECAST
    @GetMapping("/weekly")
    public Object getWeeklyWeather(
            @RequestParam double lat,
            @RequestParam double lon
    ) {
        return weatherService.getWeeklyWeather(lat, lon);
    }

}
