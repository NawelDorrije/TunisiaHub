package org.example.backend_tunisiahub.Services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Configs.AiRecommendationProperties;
import org.example.backend_tunisiahub.Controllers.dto.AiReservationSuggestionResponse;
import org.example.backend_tunisiahub.Controllers.dto.HourlyReservationCountDto;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendationService {

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_OPENAI_MODEL = "gpt-4o-mini";
    private static final String OPENAI_SYSTEM_PROMPT = """
            You are an assistant helping users choose the best restaurant reservation time.
            Analyze the reservation counts per hour and reply with JSON only.
            Be friendly, concise, and practical.
            Prefer the least crowded valid time slot.
            The JSON schema is:
            {
              "bestTime": "HH:mm",
              "message": "short explanation"
            }
            """;

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final AiRecommendationProperties properties;

    public AiReservationSuggestionResponse suggestBestTime(Long restaurantId, LocalDate date) {
        validateInputs(restaurantId, date);

        List<Reservation> reservations = reservationRepository.findForRestaurantSuggestionDay(
                restaurantId,
                ReservationType.RestaurantReservation,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(),
                ReservationStatus.CANCELLED
        );

        List<HourlyReservationCountDto> hourlyCounts = aggregateByHour(reservations);
        if (hourlyCounts.isEmpty()) {
            return new AiReservationSuggestionResponse(null,
                    "No reservations found for this restaurant on " + date + ".");
        }

        AiReservationSuggestionResponse fallback = buildFallbackSuggestion(hourlyCounts);

        if (!StringUtils.hasText(properties.apiKey())) {
            log.warn("OPENAI_API_KEY is not configured. Using fallback reservation suggestion.");
            return fallback;
        }

        try {
            return requestAiSuggestion(date, hourlyCounts, fallback);
        } catch (RestClientException | JsonProcessingException ex) {
            log.warn("OpenAI reservation suggestion failed. Falling back to basic logic.", ex);
            return fallback;
        } catch (Exception ex) {
            log.warn("Unexpected AI suggestion failure. Falling back to basic logic.", ex);
            return fallback;
        }
    }

    private void validateInputs(Long restaurantId, LocalDate date) {
        if (restaurantId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "restaurantId is required");
        }
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "date is required");
        }
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }
    }

    private List<HourlyReservationCountDto> aggregateByHour(List<Reservation> reservations) {
        Map<LocalTime, Long> countsByHour = new TreeMap<>();

        for (Reservation reservation : reservations) {
            if (reservation.getDateTime() == null) {
                continue;
            }
            LocalTime hour = reservation.getDateTime().toLocalTime().withMinute(0).withSecond(0).withNano(0);
            countsByHour.merge(hour, 1L, Long::sum);
        }

        return countsByHour.entrySet().stream()
                .map(entry -> new HourlyReservationCountDto(entry.getKey().format(HOUR_FORMATTER), entry.getValue()))
                .toList();
    }

    private AiReservationSuggestionResponse buildFallbackSuggestion(List<HourlyReservationCountDto> hourlyCounts) {
        HourlyReservationCountDto leastBusy = hourlyCounts.stream()
                .min(Comparator.comparingLong(HourlyReservationCountDto::reservations)
                        .thenComparing(HourlyReservationCountDto::hour))
                .orElseThrow();

        return new AiReservationSuggestionResponse(
                leastBusy.hour(),
                leastBusy.hour() + " is currently the least busy time."
        );
    }

    private AiReservationSuggestionResponse requestAiSuggestion(LocalDate date,
                                                                List<HourlyReservationCountDto> hourlyCounts,
                                                                AiReservationSuggestionResponse fallback) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.apiKey());

        OpenAiResponseRequest request = new OpenAiResponseRequest(
                resolveModel(),
                List.of(
                        new OpenAiInputMessage(
                                "system",
                                List.of(new OpenAiTextInput("input_text", OPENAI_SYSTEM_PROMPT))
                        ),
                        new OpenAiInputMessage(
                                "user",
                                List.of(new OpenAiTextInput("input_text", buildUserPrompt(date, hourlyCounts)))
                        )
                ),
                new OpenAiTextConfig(
                        new OpenAiJsonSchemaFormat(
                                "json_schema",
                                "reservation_suggestion",
                                "Reservation suggestion result",
                                true,
                                new OpenAiSchema(
                                        "object",
                                        Map.of(
                                                "bestTime", Map.of("type", "string"),
                                                "message", Map.of("type", "string")
                                        ),
                                        List.of("bestTime", "message"),
                                        false
                                )
                        )
                )
        );

        HttpEntity<OpenAiResponseRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<OpenAiResponseEnvelope> response = restTemplate.exchange(
                resolveBaseUrl() + "/responses",
                HttpMethod.POST,
                entity,
                OpenAiResponseEnvelope.class
        );

        OpenAiResponseEnvelope body = response.getBody();
        String rawJson = extractOutputText(body);
        if (!StringUtils.hasText(rawJson)) {
            return fallback;
        }

        AiReservationSuggestionResponse aiResponse =
                objectMapper.readValue(sanitizeJson(rawJson), AiReservationSuggestionResponse.class);

        Set<String> availableHours = hourlyCounts.stream()
                .map(HourlyReservationCountDto::hour)
                .collect(java.util.stream.Collectors.toSet());

        if (!StringUtils.hasText(aiResponse.bestTime())
                || !StringUtils.hasText(aiResponse.message())
                || !availableHours.contains(aiResponse.bestTime())) {
            return fallback;
        }

        return aiResponse;
    }

    private String buildUserPrompt(LocalDate date, List<HourlyReservationCountDto> hourlyCounts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Reservation date: ").append(date).append('\n');
        prompt.append("Based on the following reservation counts per hour, recommend the best time slot and explain why in a friendly and concise way.\n");

        for (HourlyReservationCountDto count : hourlyCounts) {
            prompt.append(count.hour())
                    .append(" -> ")
                    .append(count.reservations())
                    .append('\n');
        }

        prompt.append("Return JSON only.");
        return prompt.toString();
    }

    private String extractOutputText(OpenAiResponseEnvelope response) {
        if (response == null || response.output() == null) {
            return null;
        }

        for (OpenAiOutputItem outputItem : response.output()) {
            if (outputItem.content() == null) {
                continue;
            }
            for (OpenAiOutputContent content : outputItem.content()) {
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

    private String resolveBaseUrl() {
        return StringUtils.hasText(properties.baseUrl()) ? properties.baseUrl() : DEFAULT_OPENAI_BASE_URL;
    }

    private String resolveModel() {
        return StringUtils.hasText(properties.model()) ? properties.model() : DEFAULT_OPENAI_MODEL;
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
