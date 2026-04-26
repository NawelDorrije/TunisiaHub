package org.example.backend_tunisiahub.Services.Restaurant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchRequest;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchResponse;
import org.example.backend_tunisiahub.Controllers.Restaurant.dto.AiSearchResult;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class AiSearchService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String SYSTEM_PROMPT = """
            You are an AI restaurant search and ranking engine for TunisiaHub.
            Your task is to analyze a user's restaurant request and rank only the provided candidate restaurants.

            Current server date/time: %s

            Language handling:
            - Understand and respond correctly to English, French, and Arabic queries.
            - The user's query may mix languages.
            - Keep the JSON values natural and concise. The "message" and "reason" fields should follow the user's language when it is obvious; otherwise use English.

            Ranking priorities, from strongest to weakest:
            1. Time suitability and likely availability for the requested meal or moment.
            2. Party size clues from the query.
            3. Cuisine preference.
            4. Dietary constraints and food preferences such as gluten-free, vegetarian, vegan, halal, low-carb, seafood, or kid-friendly options.
            5. Distance and proximity when user coordinates are available.
            6. Ambience / vibe such as romantic, quiet, luxury, family-friendly, business, casual, cozy, trendy, rooftop, view, or fast service.

            Hard rules:
            - Use only the candidate restaurants provided by the application.
            - Never invent restaurant IDs or restaurants not present in the input.
            - Return ONLY valid JSON.
            - Do not use markdown.
            - Do not wrap the JSON in code fences.
            - Do not add explanations before or after the JSON.
            - "matchScore" must be an integer between 0 and 100.
            - "totalMatches" must equal the number of objects in "results".
            - Sort results from best match to worst match.
            - Keep "reason" short, specific, and user-facing.
            - Use "suggestedTime" when the query implies a meal, date, or period; otherwise provide a sensible suggestion such as "19:30" or "12:30".
            - Use "people" to estimate party size from the query. If not stated, infer a reasonable default, usually 2 for date/romantic queries or 1 otherwise.

            Output JSON schema:
            {
              "message": "short summary",
              "results": [
                {
                  "restaurantId": 123,
                  "matchScore": 92,
                  "reason": "short explanation",
                  "suggestedTime": "19:30",
                  "people": 2
                }
              ],
              "totalMatches": 1
            }
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiSearchService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public AiSearchResponse search(AiSearchRequest request, List<Restaurant> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return new AiSearchResponse("No matching restaurants found.", List.of(), 0);
        }

        try {
            String rawContent = chatClient.prompt()
                    .system(SYSTEM_PROMPT.formatted(LocalDateTime.now().format(DATE_TIME_FORMATTER)))
                    .user(buildUserPrompt(request, candidates))
                    .call()
                    .content();

            return parseResponse(rawContent);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI restaurant search failed", ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI restaurant search failed");
        }
    }

    private String buildUserPrompt(AiSearchRequest request, List<Restaurant> candidates) {
        StringBuilder builder = new StringBuilder();
        builder.append("User query:\n");
        builder.append(request.query()).append("\n\n");
        builder.append("User coordinates:\n");
        builder.append("- latitude: ")
                .append(request.latitude().map(value -> String.format(Locale.US, "%.6f", value)).orElse("not provided"))
                .append('\n');
        builder.append("- longitude: ")
                .append(request.longitude().map(value -> String.format(Locale.US, "%.6f", value)).orElse("not provided"))
                .append("\n\n");
        builder.append("Candidate restaurants:\n");

        for (Restaurant candidate : candidates) {
            builder.append("{")
                    .append("\"restaurantId\": ").append(candidate.getId()).append(", ")
                    .append("\"name\": ").append(toJsonString(valueOrUnknown(candidate.getName()))).append(", ")
                    .append("\"address\": ").append(toJsonString(valueOrUnknown(candidate.getAddress()))).append(", ")
                    .append("\"cuisine\": ").append(toJsonString(candidate.getCuisine() == null ? "unknown" : candidate.getCuisine().name())).append(", ")
                    .append("\"latitude\": ").append(candidate.getLatitude() == null ? "null" : String.format(Locale.US, "%.6f", candidate.getLatitude())).append(", ")
                    .append("\"longitude\": ").append(candidate.getLongitude() == null ? "null" : String.format(Locale.US, "%.6f", candidate.getLongitude()))
                    .append("}\n");
        }

        builder.append("\nReturn only JSON matching the required schema.");
        return builder.toString();
    }

    private AiSearchResponse parseResponse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI returned an empty response");
        }

        String sanitized = sanitizeJson(rawContent);

        try {
            AiSearchResponse parsed = objectMapper.readValue(sanitized, AiSearchResponse.class);
            List<AiSearchResult> results = parsed.results() == null ? List.of() : List.copyOf(parsed.results());
            int totalMatches = parsed.totalMatches() != results.size() ? results.size() : parsed.totalMatches();
            String message = parsed.message() == null || parsed.message().isBlank()
                    ? "AI search completed."
                    : parsed.message();
            return new AiSearchResponse(message, results, totalMatches);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse AI search response JSON: {}", sanitized, ex);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI returned invalid JSON");
        }
    }

    private String sanitizeJson(String rawContent) {
        String sanitized = rawContent.trim();
        if (sanitized.startsWith("```")) {
            sanitized = sanitized.replaceFirst("^```(?:json)?\\s*", "");
            sanitized = sanitized.replaceFirst("\\s*```$", "");
        }
        return sanitized.trim();
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to serialize AI candidate payload");
        }
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
