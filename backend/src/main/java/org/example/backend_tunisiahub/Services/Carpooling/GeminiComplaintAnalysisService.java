package org.example.backend_tunisiahub.Services.Carpooling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiComplaintAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiComplaintAnalysisService.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.0-flash}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ComplaintAiAnalysis analyze(String description) {
        if (description == null || description.isBlank()) {
            return new ComplaintAiAnalysis("", "", "");
        }

        if (apiKey == null || apiKey.isBlank()) {
            return new ComplaintAiAnalysis(
                    "Gemini API key is missing.",
                    "configuration, api key",
                    "Add GEMINI_API_KEY to your environment or set gemini.api.key in application.properties."
            );
        }

        try {
            String prompt = buildPrompt(description);
            Map<String, Object> body = Map.of(
                    "contents",
                    List.of(Map.of(
                            "parts",
                            List.of(Map.of("text", prompt))
                    )),
                    "generationConfig",
                    Map.of(
                            "temperature", 0.2,
                            "maxOutputTokens", 350
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model
                    + ":generateContent?key="
                    + apiKey;

            String response = restTemplate.postForObject(url, entity, String.class);
            return completeAnalysis(parseResponse(response, description), description);
        } catch (HttpStatusCodeException exception) {
            logger.error(
                    "Gemini request failed status={} body={}",
                    exception.getStatusCode(),
                    exception.getResponseBodyAsString()
            );
            if (exception.getStatusCode().value() == 503 || exception.getStatusCode().value() == 429) {
                return simpleLocalAnalysis(description);
            }
            return buildHttpErrorAnalysis(exception);
        } catch (Exception exception) {
            logger.error("Gemini analysis failed", exception);
            return new ComplaintAiAnalysis(
                    "AI analysis failed.",
                    "ai, error",
                    "Check backend logs for the exact Gemini error."
            );
        }
    }

    private String buildPrompt(String description) {
        return "Analyze this carpooling complaint for an admin dashboard. "
                + "The answer must help the admin decide quickly. "
                + "Do not use JSON. Do not use markdown. Do not use code blocks. "
                + "Return exactly 3 lines in this format: "
                + "SUMMARY=DECISION; problem 1; problem 2; problem 3. "
                + "KEYWORDS=keyword 1, keyword 2, keyword 3, keyword 4. "
                + "SOLUTIONS=action 1; action 2; action 3. "
                + "DECISION must be one of: SUSPEND DRIVER, WARN DRIVER, ASK DETAILS, REVIEW. "
                + "Use 3 or 4 short bullet points after SUMMARY and SOLUTIONS. "
                + "Report description: "
                + description;
    }

    private ComplaintAiAnalysis parseResponse(String response, String description) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String text = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText("");

        if (text == null || text.isBlank()) {
            return new ComplaintAiAnalysis(
                    "Gemini returned an empty response.",
                    "ai, empty response",
                    "Try again or check the Gemini API response in backend logs."
            );
        }

        String cleaned = cleanJsonText(text);
        JsonNode result;
        try {
            result = objectMapper.readTree(cleaned);
            return new ComplaintAiAnalysis(
                    nodeToText(result.path("summary")),
                    nodeToText(result.path("keywords")),
                    nodeToText(result.path("solutions"))
            );
        } catch (Exception exception) {
            ComplaintAiAnalysis analysis = parseTextResponse(cleaned);
            if (analysis != null) {
                return analysis;
            }

            logger.warn("Gemini returned text with unknown format: {}", text);
            return simpleLocalAnalysis(description);
        }
    }

    private ComplaintAiAnalysis parseTextResponse(String text) {
        String summary = "";
        String keywords = "";
        String solutions = "";
        String[] lines = text.split("\\R");

        for (String line : lines) {
            String value = line.trim();
            if (value.toLowerCase().startsWith("summary=")) {
                summary = value.substring("summary=".length()).trim();
            } else if (value.toLowerCase().startsWith("keywords=")) {
                keywords = value.substring("keywords=".length()).trim();
            } else if (value.toLowerCase().startsWith("solutions=")) {
                solutions = value.substring("solutions=".length()).trim();
            }
        }

        if (!summary.isBlank() || !keywords.isBlank() || !solutions.isBlank()) {
            return new ComplaintAiAnalysis(summary, keywords, solutions);
        }

        return null;
    }

    private ComplaintAiAnalysis simpleLocalAnalysis(String description) {
        String text = description == null ? "" : description.toLowerCase();

        if (text.contains("phone") || text.contains("speed") || text.contains("overtaking") || text.contains("seatbelt")) {
            return new ComplaintAiAnalysis(
                    "SUSPEND DRIVER; unsafe driving or vehicle safety issue; passenger felt in danger; driver ignored safety concern",
                    "unsafe driving, safety, driver misconduct",
                    "Contact passenger for confirmation; Suspend driver during review; Check driver history"
            );
        }

        if (text.contains("cash") || text.contains("extra") || text.contains("pickup") || text.contains("location")) {
            return new ComplaintAiAnalysis(
                    "WARN DRIVER; pickup or payment rule problem; passenger was pressured; trip rules were not respected",
                    "extra payment, pickup change, driver misconduct",
                    "Contact driver for explanation; Warn driver about policy; Refund passenger if needed"
            );
        }

        return new ComplaintAiAnalysis(
                "ASK DETAILS; report is unclear; no specific safety issue found; needs manual review",
                "unclear report, manual review",
                "Ask passenger for clear details; Check trip and reservation; Decide manually"
        );
    }

    private ComplaintAiAnalysis completeAnalysis(ComplaintAiAnalysis analysis, String description) {
        ComplaintAiAnalysis fallback = simpleLocalAnalysis(description);
        return new ComplaintAiAnalysis(
                isBlank(analysis.summary()) ? fallback.summary() : analysis.summary(),
                isBlank(analysis.keywords()) ? fallback.keywords() : analysis.keywords(),
                isBlank(analysis.solutions()) ? fallback.solutions() : analysis.solutions()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank() || value.trim().equals("-");
    }

    private String nodeToText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }
        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : node) {
                String value = nodeToText(item);
                if (!value.isBlank()) {
                    if (!builder.isEmpty()) {
                        builder.append("; ");
                    }
                    builder.append(value);
                }
            }
            return builder.toString();
        }
        if (node.isObject()) {
            StringBuilder builder = new StringBuilder();
            node.fields().forEachRemaining((entry) -> {
                String value = nodeToText(entry.getValue());
                if (!value.isBlank()) {
                    if (!builder.isEmpty()) {
                        builder.append("; ");
                    }
                    builder.append(entry.getKey()).append(": ").append(value);
                }
            });
            return builder.toString();
        }
        return node.asText("");
    }

    private String cleanJsonText(String text) {
        String value = text == null ? "" : text.trim();
        if (value.startsWith("```json")) {
            value = value.substring(7);
        }
        if (value.startsWith("```")) {
            value = value.substring(3);
        }
        if (value.endsWith("```")) {
            value = value.substring(0, value.length() - 3);
        }

        int firstBrace = value.indexOf('{');
        int lastBrace = value.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            value = value.substring(firstBrace, lastBrace + 1);
        }

        return value.trim();
    }

    private ComplaintAiAnalysis buildHttpErrorAnalysis(HttpStatusCodeException exception) {
        int status = exception.getStatusCode().value();
        String summary = "Gemini request failed with HTTP " + status + ".";
        String keywords = "gemini, api, http " + status;
        String solutions = "Check the Gemini API key and model name.";

        if (status == 400) {
            solutions = "Check the model name and request body. Try GEMINI_MODEL=gemini-1.5-flash if this model is not accepted.";
        } else if (status == 403) {
            solutions = "Check that the API key is valid and that the Generative Language API is enabled for the Google project.";
        } else if (status == 404) {
            solutions = "The model name was not found. Try GEMINI_MODEL=gemini-1.5-flash or another model available to your API key.";
        } else if (status == 429) {
            solutions = "Gemini quota was exceeded. Wait, reduce requests, or check your Google AI quota.";
        }

        return new ComplaintAiAnalysis(summary, keywords, solutions);
    }
}
