package org.example.backend_tunisiahub.Services.Event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
@Slf4j
public class GeminiVisionService implements GeminiService {

    private static final String DEFAULT_DESCRIPTION = "This event image presents a setting worth discovering. Add details about the program, audience, and atmosphere.";

    private final WebClient webClient;
    private final String apiKey;
    private final List<String> modelCandidates;

    public GeminiVisionService(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.models:gemini-2.5-flash,gemini-2.0-flash}") String modelNames
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.modelCandidates = Arrays.stream(modelNames.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    @Override
    public String generateEventDescription(MultipartFile imageFile, String imageUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is missing. Returning fallback description.");
            return DEFAULT_DESCRIPTION;
        }

        String mimeType = imageFile.getContentType() != null ? imageFile.getContentType() : "image/jpeg";
        try {
            String imageBase64 = Base64.getEncoder().encodeToString(imageFile.getBytes());

            Map<String, Object> textPart = Map.of(
                    "text", "Create a concise event-ready description for this image in 2-3 sentences. " +
                            "Keep it factual, attractive, and suitable for an event listing. " +
                            "Do not use bullet points."
            );

            Map<String, Object> imagePart = Map.of(
                    "inline_data", Map.of(
                            "mime_type", mimeType,
                            "data", imageBase64
                    )
            );

            Map<String, Object> request = new HashMap<>();
            request.put("contents", List.of(Map.of("parts", List.of(textPart, imagePart))));
            request.put("generationConfig", Map.of(
                    "temperature", 0.3,
                    "maxOutputTokens", 180
            ));

            for (String model : modelCandidates) {
                try {
                    JsonNode response = webClient.post()
                            .uri(uriBuilder -> uriBuilder
                                    .path("/v1beta/models/{model}:generateContent")
                                    .queryParam("key", apiKey)
                                    .build(model))
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .block();

                    String generatedText = extractGeneratedText(response);
                    if (generatedText != null && !generatedText.isBlank()) {
                        return generatedText.trim();
                    }

                    log.warn("Gemini returned empty description for model={}", model);
                } catch (WebClientResponseException.NotFound notFound) {
                    log.warn("Gemini model not found: {}. Trying next configured model.", model);
                } catch (Exception ex) {
                    log.warn("Gemini request failed for model={}. Trying next configured model.", model, ex);
                }
            }

            log.error("All configured Gemini models failed. Returning fallback description.");
            return DEFAULT_DESCRIPTION;
        } catch (Exception ex) {
            log.error("Gemini description generation failed before request for imageUrl={} mimeType={}", imageUrl, mimeType, ex);
            return DEFAULT_DESCRIPTION;
        }
    }

    private String extractGeneratedText(JsonNode response) {
        if (response == null) {
            return null;
        }

        JsonNode candidates = response.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) {
            return null;
        }

        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return null;
        }

        for (JsonNode part : parts) {
            String text = part.path("text").asText("");
            if (!text.isBlank()) {
                return text;
            }
        }

        return null;
    }
}
