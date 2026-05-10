package org.example.backend_tunisiahub.Services.Event;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Event.Event;
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
    private static final String DEFAULT_MARKETING_DESCRIPTION = "Discover this unique event and reserve your place now for an unforgettable experience.";
    private static final String DEFAULT_POSTER_URL = "https://image.pollinations.ai/prompt/modern%20event%20poster%20minimal%20design%20high%20quality";

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

    @Override
    public String generateMarketingDescription(Event event) {
        if (event == null) {
            return DEFAULT_MARKETING_DESCRIPTION;
        }

        String fallback = buildFallbackMarketingDescription(event);
        if (apiKey == null || apiKey.isBlank()) {
            return fallback;
        }

        String prompt = """
                Write a compelling event marketing description in 3-4 short sentences.
                Keep the tone modern, energetic, and conversion-focused.
                Include these factual details naturally without changing numbers:
                - Title: %s
                - Start date: %s
                - End date: %s
                - Price: %.2f TND
                - Capacity: %d
                - Location: %s
                """.formatted(
                safeText(event.getTitle()),
                safeText(event.getStartDate()),
                safeText(event.getEndDate()),
                event.getPrice(),
                event.getCapacity(),
                safeText(event.getLieu())
        );

        String generated = generateTextContent(prompt, 0.55, 220);
        return generated == null || generated.isBlank() ? fallback : generated.trim();
    }

    @Override
    public String generatePosterImage(Event event) {
        if (event == null) {
            return DEFAULT_POSTER_URL;
        }

        String fallbackPrompt = buildPosterPrompt(event);
        String promptFromGemini = fallbackPrompt;

        if (apiKey != null && !apiKey.isBlank()) {
            String improvePrompt = """
                    Create one concise prompt for an AI image generator.
                    Goal: modern event poster, clean UI, attractive, minimalistic design.
                    Must include: title, date, location, price.
                    Keep it under 320 characters.
                    Event data:
                    Title: %s
                    Start date: %s
                    End date: %s
                    Location: %s
                    Price: %.2f TND
                    Capacity: %d
                    """.formatted(
                    safeText(event.getTitle()),
                    safeText(event.getStartDate()),
                    safeText(event.getEndDate()),
                    safeText(event.getLieu()),
                    event.getPrice(),
                    event.getCapacity()
            );

            String generatedPrompt = generateTextContent(improvePrompt, 0.6, 180);
            if (generatedPrompt != null && !generatedPrompt.isBlank()) {
                promptFromGemini = generatedPrompt.trim();
            }
        }

        String encodedPrompt = java.net.URLEncoder.encode(promptFromGemini, java.nio.charset.StandardCharsets.UTF_8);
        return "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=1024&height=1536&nologo=true";
    }

    private String generateTextContent(String prompt, double temperature, int maxOutputTokens) {
        Map<String, Object> request = new HashMap<>();
        request.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
        )));
        request.put("generationConfig", Map.of(
                "temperature", temperature,
                "maxOutputTokens", maxOutputTokens
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
                    return generatedText;
                }
            } catch (WebClientResponseException.NotFound notFound) {
                log.warn("Gemini model not found: {}. Trying next configured model.", model);
            } catch (Exception ex) {
                log.warn("Gemini request failed for model={}. Trying next configured model.", model, ex);
            }
        }
        return null;
    }

    private String buildPosterPrompt(Event event) {
        return "modern event poster, clean UI, attractive, include title "
                + safeText(event.getTitle())
                + ", date " + safeText(event.getStartDate())
                + ", location " + safeText(event.getLieu())
                + ", price " + String.format("%.2f TND", event.getPrice())
                + ", minimalistic design";
    }

    private String buildFallbackMarketingDescription(Event event) {
        return ("Join " + safeText(event.getTitle())
                + " at " + safeText(event.getLieu())
                + " from " + safeText(event.getStartDate())
                + " to " + safeText(event.getEndDate())
                + ". Tickets are " + String.format("%.2f TND", event.getPrice())
                + " with a limited capacity of " + event.getCapacity()
                + " guests. Reserve now and be part of a standout experience.").trim();
    }

    private String safeText(Object value) {
        return value == null ? "N/A" : value.toString();
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
