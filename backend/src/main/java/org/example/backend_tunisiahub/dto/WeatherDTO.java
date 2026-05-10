package org.example.backend_tunisiahub.dto;

import lombok.Data;

@Data
public class WeatherDTO {
    private double temperature;
    private double windSpeed;
    private int humidity;
    private double precipitation;
    private String description;
}
