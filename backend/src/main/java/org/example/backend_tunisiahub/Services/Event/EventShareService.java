package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventShareService {

    private final EventRepository eventRepository;
    private final GeminiService geminiService;

    public Event getEventReadyForShare(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found."));

        boolean changed = false;

        if (event.getMarketingDescription() == null || event.getMarketingDescription().isBlank()) {
            event.setMarketingDescription(generateMarketingDescriptionSafely(event));
            changed = true;
        }

        if (event.getPosterImageUrl() == null || event.getPosterImageUrl().isBlank()) {
            event.setPosterImageUrl(generatePosterImageSafely(event));
            changed = true;
        }

        if (changed) {
            event = eventRepository.save(event);
        }

        return event;
    }

    private String generateMarketingDescriptionSafely(Event event) {
        try {
            String generated = geminiService.generateMarketingDescription(event);
            if (generated != null && !generated.isBlank()) {
                return generated;
            }
        } catch (Exception ex) {
            log.warn("Gemini marketing description failed for eventId={}. Using fallback text.", event.getId(), ex);
        }

        String start = event.getStartDate() == null ? "N/A" : event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String end = event.getEndDate() == null ? "N/A" : event.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String title = safe(event.getTitle());
        String lieu = safe(event.getLieu());
        String baseDescription = safe(event.getDescription());

        return ("Event: " + title + ". "
                + baseDescription + " "
                + "Date: " + start + " -> " + end + ". "
                + "Location: " + lieu + ".").trim();
    }

    private String generatePosterImageSafely(Event event) {
        try {
            String generated = geminiService.generatePosterImage(event);
            if (generated != null && !generated.isBlank()) {
                return generated;
            }
        } catch (Exception ex) {
            log.warn("Gemini poster generation failed for eventId={}. Using fallback image.", event.getId(), ex);
        }

        if (event.getImage() != null && !event.getImage().isBlank()) {
            return event.getImage();
        }

        String fallbackPrompt = "event poster " + safe(event.getTitle()) + " " + safe(event.getLieu());
        return "https://image.pollinations.ai/prompt/"
                + URLEncoder.encode(fallbackPrompt, StandardCharsets.UTF_8)
                + "?width=1024&height=1536&nologo=true";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
