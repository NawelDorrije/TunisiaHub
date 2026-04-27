package org.example.backend_tunisiahub.Services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Configs.AiRecommendationProperties;
import org.example.backend_tunisiahub.Entities.Restaurant.Cuisine;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";
    private static final String EXPLANATION_SYSTEM_PROMPT = """
            You write short restaurant recommendation explanations.
            Return JSON only using this schema:
            {
              "reason": "one friendly sentence"
            }
            Keep the tone warm, concise, and natural.
            Do not mention internal analytics, scores, or technical details.
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiRecommendationProperties properties;

    public String generateRecommendationReason(Cuisine cuisine,
                                               String priceRange,
                                               String preferredTime,
                                               boolean hasHistory,
                                               String fallbackReason) {
        if (!StringUtils.hasText(properties.apiKey())) {
            return fallbackReason;
        }

        try {
            return requestReason(cuisine, priceRange, preferredTime, hasHistory, fallbackReason);
        } catch (RestClientException | JsonProcessingException ex) {
            log.warn("OpenAI explanation generation failed. Using fallback reason.", ex);
            return fallbackReason;
        } catch (Exception ex) {
            log.warn("Unexpected explanation generation failure. Using fallback reason.", ex);
            return fallbackReason;
        }
    }

    private String requestReason(Cuisine cuisine,
                                 String priceRange,
                                 String preferredTime,
                                 boolean hasHistory,
                                 String fallbackReason) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiKey());

        OpenAiResponseRequest request = new OpenAiResponseRequest(
                resolveModel(),
                List.of(
                        new OpenAiInputMessage(
                                "system",
                                List.of(new OpenAiTextInput("input_text", EXPLANATION_SYSTEM_PROMPT))
                        ),
                        new OpenAiInputMessage(
                                "user",
                                List.of(new OpenAiTextInput("input_text",
                                        buildPrompt(cuisine, priceRange, preferredTime, hasHistory, fallbackReason)))
                        )
                ),
                new OpenAiTextConfig(
                        new OpenAiJsonSchemaFormat(
                                "json_schema",
                                "restaurant_recommendation_reason",
                                "Restaurant recommendation explanation",
                                true,
                                new OpenAiSchema(
                                        "object",
                                        Map.of("reason", Map.of("type", "string")),
                                        List.of("reason"),
                                        false
                                )
                        )
                )
        );

        ResponseEntity<OpenAiResponseEnvelope> response = restTemplate.exchange(
                resolveBaseUrl() + "/responses",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                OpenAiResponseEnvelope.class
        );

        String outputText = extractOutputText(response.getBody());
        if (!StringUtils.hasText(outputText)) {
            return fallbackReason;
        }

        ExplanationResponse explanationResponse =
                objectMapper.readValue(sanitizeJson(outputText), ExplanationResponse.class);

        if (!StringUtils.hasText(explanationResponse.reason())) {
            return fallbackReason;
        }

        return explanationResponse.reason().trim();
    }

    private String buildPrompt(Cuisine cuisine,
                               String priceRange,
                               String preferredTime,
                               boolean hasHistory,
                               String fallbackReason) {
        if (!hasHistory) {
            return """
                    The user has no reservation history.
                    Generate one short recommendation reason for top-rated restaurants.
                    Keep it friendly and generic.
                    Reference idea: Popular restaurants near you.
                    Return JSON only.
                    """;
        }

        String cuisineLabel = cuisine == null ? "different cuisines" : formatCuisine(cuisine);
        String timeLabel = StringUtils.hasText(preferredTime) ? preferredTime.toLowerCase() : "their usual outings";
        String priceLabel = StringUtils.hasText(priceRange) ? priceRange.toLowerCase().replace('_', ' ') : "a similar budget";

        return """
                The user prefers %s, usually books for %s, and tends to choose %s options.
                Generate one short, friendly recommendation reason.
                Keep it natural and avoid mentioning data analysis.
                Fallback meaning: %s
                Return JSON only.
                """.formatted(cuisineLabel, timeLabel, priceLabel, fallbackReason);
    }

    private String extractOutputText(OpenAiResponseEnvelope response) {
        if (response == null || response.output() == null) {
            return null;
        }

        for (OpenAiOutputItem item : response.output()) {
            if (item.content() == null) {
                continue;
            }
            for (OpenAiOutputContent content : item.content()) {
                if ("output_text".equals(content.type()) && StringUtils.hasText(content.text())) {
                    return content.text();
                }
            }
        }
        return null;
    }

    private String sanitizeJson(String rawJson) {
        String sanitized = rawJson.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```(?:json)?\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }

    private String formatCuisine(Cuisine cuisine) {
        return cuisine.name().toLowerCase().replace('_', ' ');
    }

    private String resolveBaseUrl() {
        return StringUtils.hasText(properties.baseUrl()) ? properties.baseUrl() : DEFAULT_OPENAI_BASE_URL;
    }

    private String resolveModel() {
        return StringUtils.hasText(properties.model()) ? properties.model() : DEFAULT_OPENAI_MODEL;
    }

    private record ExplanationResponse(String reason) {
    }

    private record OpenAiResponseRequest(
            String model,
            List<OpenAiInputMessage> input,
            OpenAiTextConfig text
    ) {
    }

    private record OpenAiInputMessage(
            String role,
            List<OpenAiTextInput> content
    ) {
    }

    private record OpenAiTextInput(
            String type,
            String text
    ) {
    }

    private record OpenAiTextConfig(OpenAiJsonSchemaFormat format) {
    }

    private record OpenAiJsonSchemaFormat(
            String type,
            String name,
            String description,
            boolean strict,
            OpenAiSchema schema
    ) {
    }

    private record OpenAiSchema(
            String type,
            Map<String, Map<String, String>> properties,
            List<String> required,
            @JsonProperty("additionalProperties") boolean additionalProperties
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiResponseEnvelope(List<OpenAiOutputItem> output) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiOutputItem(List<OpenAiOutputContent> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenAiOutputContent(String type, String text) {
    }
}
