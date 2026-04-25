package org.example.backend_tunisiahub.Controllers.Event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Services.Event.AiEventRecommendationService;
import org.example.backend_tunisiahub.dto.event.AiEventChatRequest;
import org.example.backend_tunisiahub.dto.event.AiEventChatResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/ai/events")
@RequiredArgsConstructor
@Slf4j
public class AiEventRecommendationController {

    private final AiEventRecommendationService aiEventRecommendationService;

    @PostMapping("/chat")
    public AiEventChatResponse chat(@Valid @RequestBody AiEventChatRequest request) {
        try {
            return aiEventRecommendationService.recommendEvents(request.message());
        } catch (Exception exception) {
            log.error("Safe fallback triggered for /ai/events/chat", exception);
            return new AiEventChatResponse("No events found or error handled safely", java.util.List.of());
        }
    }
}
