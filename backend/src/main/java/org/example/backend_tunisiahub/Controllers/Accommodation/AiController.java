package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.UserHistory;
import org.example.backend_tunisiahub.Services.Accommodation.AccommodationService;
import org.example.backend_tunisiahub.Services.Accommodation.UserHistoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final RestTemplate restTemplate;
    private final UserHistoryService userHistoryService;
    private final AccommodationService accommodationService;
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                aiServiceUrl + "/chat",
                request,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "response", "AI service unavailable. Please try again later."
            ));
        }
    }

    @PostMapping("/ingest")
    public ResponseEntity<?> ingest() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                aiServiceUrl + "/ingest",
                null,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Failed to ingest documents."
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                aiServiceUrl + "/health",
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "AI service is down"
            ));
        }
    }
    @PostMapping("/suggest-price")
    public ResponseEntity<?> suggestPrice(@RequestBody Map<String, Object> request) {
    try {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            aiServiceUrl + "/suggest-price",
            request,
            Map.class
        );
        return ResponseEntity.ok(response.getBody());
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of(
            "recommended", 100,
            "reasoning", "AI service unavailable."
        ));
    }
}
    @PostMapping("/generate-description")
    public ResponseEntity<?> generateDescription(@RequestBody Map<String, Object> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/generate-description",
                    request,
                    Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "description", "AI service unavailable."
            ));
        }
    }
    @PostMapping("/moderate-review")
    public ResponseEntity<?> moderateReview(@RequestBody Map<String, Object> request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                aiServiceUrl + "/moderate-review",
                request,
                Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            // If AI service down → allow review
            return ResponseEntity.ok(Map.of(
                "is_appropriate", true,
                "reason", "Moderation unavailable."
            ));
        }
    }
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @AuthenticationPrincipal String email) {
        try {
            // Get user history
            List<UserHistory> history = userHistoryService.getUserHistory(email);

            if (history.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            // Build history text for AI
            StringBuilder historyText = new StringBuilder();
            for (UserHistory h : history) {
                Accommodation a = h.getAccommodation();
                historyText.append(String.format(
                        "- %s (%s) | Location: %s | Price: %.0f TND | Capacity: %d persons\n",
                        a.getTitle(), a.getType(), a.getAdresse(),
                        a.getPrice(), a.getCapacite()
                ));
            }

            // Get all accommodations
            List<Accommodation> all = accommodationService.retrieveAllAccommodations();
            StringBuilder allText = new StringBuilder();
            for (Accommodation a : all) {
                allText.append(String.format(
                        "ID:%d | %s (%s) | Location: %s | Price: %.0f TND | Capacity: %d\n",
                        a.getId(), a.getTitle(), a.getType(),
                        a.getAdresse(), a.getPrice(), a.getCapacite()
                ));
            }

            // Call Python AI service
            Map<String, String> request = Map.of(
                    "history", historyText.toString(),
                    "all_accommodations", allText.toString()
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    aiServiceUrl + "/recommend",
                    request,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("recommendations", List.of()));
        }
    }
}