package org.example.backend_tunisiahub.Controllers.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Services.Event.EventSlotRecommendationService;
import org.example.backend_tunisiahub.dto.event.EventSlotRecommendationRequest;
import org.example.backend_tunisiahub.dto.event.EventSlotRecommendationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventRecommendationController {

    private final EventSlotRecommendationService eventSlotRecommendationService;

    @PostMapping("/recommendation")
    public ResponseEntity<EventSlotRecommendationResponse> recommend(
            @RequestBody EventSlotRecommendationRequest request
    ) {
        if (request == null || request.getDate() == null || request.getTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date and time are required");
        }

        EventSlotRecommendationResponse response = eventSlotRecommendationService.recommendSlots(request);
        return ResponseEntity.ok(response);
    }
}
