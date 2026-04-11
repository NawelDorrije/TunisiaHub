package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final RestTemplate restTemplate;

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
}