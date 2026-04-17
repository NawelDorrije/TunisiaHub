package org.example.backend_tunisiahub.Services.Camping.Pricing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend_tunisiahub.Entities.Camping.PricingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * AI PRICING ENGINE — powered by Groq (FREE, no credit card)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Model  : "model": "llama-3.1-8b-instant  (fast, accurate JSON output, free tier)
 * Limits : 30 req/min · 14,400 req/day — more than enough for nightly batch
 *

 * What this class does:
 *   - Builds a structured prompt from PricingContext signals
 *   - Calls Groq's OpenAI-compatible chat endpoint
 *   - Parses the JSON response to extract a price multiplier
 *   - Returns 1.0 (no change) on any error — safe fallback
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Service
public class GroqAIPricingEngine {

    private static final String GROQ_URL      = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL         = "llama-3.1-8b-instant";
    private static final double MIN_MULT      = 0.70;
    private static final double MAX_MULT      = 2.50;
    private static final double FALLBACK      = 1.0;

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper  = new ObjectMapper();

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Ask Groq/Llama3 for a price multiplier given the pricing context.
     * Returns a raw multiplier (before guardrail clamping).
     * Returns 1.0 on any network/parse error.
     */
    public double getMultiplier(PricingContext ctx) {
        try {
            String raw = callGroq(buildSystemPrompt(), buildUserPrompt(ctx));
            return parseMultiplier(raw);
        } catch (Exception e) {
            System.err.printf("[GroqEngine] Error for spot %d: %s%n",
                    ctx.spotId(), e.getMessage());
            return FALLBACK;
        }
    }

    // ── System prompt (role/contract) ──────────────────────────────────────

    private String buildSystemPrompt() {
        return """
                You are a JSON-only revenue-optimisation engine for camping spots.
                You MUST respond with a single valid JSON object containing exactly two keys:
                  "multiplier" (a decimal number) and "reason" (a short string, max 15 words).
                No preamble, no explanation, no markdown fences. Only the JSON object.
                Example: {"multiplier": 1.25, "reason": "High weekend occupancy with local festival"}
                """;
    }

    // ── User prompt (live signals + rules) ─────────────────────────────────

    private String buildUserPrompt(PricingContext ctx) {
        return """
                Compute the optimal price multiplier for a camping spot in Tunisia.

                === Current signals ===
                base_price_TND      : %.2f
                weather_score       : %.2f   [0=severe storm → 1=perfect camping weather]
                occupancy_rate      : %.2f   [fraction of camping spots already booked, 0–1]
                demand_index        : %.2f   [bookings this week ÷ same week last year]
                local_event_nearby  : %s     [festival or public holiday within 50 km & 7 days]
                day_of_week         : %s
                days_until_check_in : %d
                is_weekend          : %s     [Friday or Saturday night]
                is_last_minute      : %s     [check-in in ≤ 3 days]

                === Business rules ===
                Allowed multiplier : %.2f – %.2f
                Target improvement : +15%% to +30%% revenue over flat pricing

                INCREASE price when:
                  - occupancy_rate > 0.70
                  - is_weekend = true
                  - local_event_nearby = true
                  - demand_index > 1.20

                DECREASE price when:
                  - occupancy_rate < 0.30
                  - weather_score < 0.40
                  - demand_index < 0.80

                LAST-MINUTE rule:
                  - If is_last_minute AND occupancy_rate < 0.40 → apply ×0.90 discount
                  - If is_last_minute AND occupancy_rate > 0.60 → keep or raise price

                NEVER exceed ×2.50 — maintains customer trust.

                Respond with valid JSON only:
                {"multiplier": <number>, "reason": "<max 15 words>"}
                """.formatted(
                ctx.basePrice().doubleValue(),
                ctx.weatherScore(),
                ctx.occupancyRate(),
                ctx.demandIndex(),
                ctx.localEventNearby(),
                ctx.dayOfWeek(),
                ctx.daysUntilCheckIn(),
                ctx.isWeekend(),
                ctx.isLastMinute(),
                MIN_MULT, MAX_MULT
        );
    }

    // ── HTTP call to Groq ──────────────────────────────────────────────────

    private String callGroq(String systemPrompt, String userPrompt) throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);  // "Authorization: Bearer gsk_..."

        Map<String, Object> body = Map.of(
                "model",       MODEL,
                "max_tokens",  200,
                "temperature", 0.2,    // low = consistent, structured output
                "messages", List.of(
                        Map.of("role", "system",  "content", systemPrompt),
                        Map.of("role", "user",    "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity(GROQ_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Groq API error: " + response.getStatusCode());
        }

        // Groq returns OpenAI-compatible format:
        // { "choices": [ { "message": { "content": "..." } } ] }
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").get(0)
                .path("message").path("content").asText();
    }

    // ── JSON parsing ───────────────────────────────────────────────────────

    private double parseMultiplier(String responseText) {
        try {
            // Strip accidental markdown fences if model adds them
            String clean = responseText
                    .replaceAll("(?s)```[a-z]*\\s*", "")
                    .replaceAll("```", "")
                    .trim();

            // Extract the JSON object even if surrounded by stray text
            int start = clean.indexOf('{');
            int end   = clean.lastIndexOf('}');
            if (start == -1 || end == -1) {
                System.err.println("[GroqEngine] No JSON found in: " + responseText);
                return FALLBACK;
            }

            JsonNode node = objectMapper.readTree(clean.substring(start, end + 1));
            double multiplier = node.path("multiplier").asDouble(FALLBACK);

            // Clamp defensively (main guardrail is in DynamicPricingService)
            return Math.max(MIN_MULT, Math.min(MAX_MULT, multiplier));

        } catch (Exception e) {
            System.err.println("[GroqEngine] Parse failed: " + responseText);
            return FALLBACK;
        }
    }
}