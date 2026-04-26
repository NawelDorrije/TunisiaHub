package org.example.backend_tunisiahub.Configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record AiRecommendationProperties(
        String apiKey,
        String model,
        String baseUrl
) {
}
