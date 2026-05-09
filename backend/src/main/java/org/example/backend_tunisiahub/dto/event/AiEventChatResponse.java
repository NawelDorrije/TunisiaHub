package org.example.backend_tunisiahub.dto.event;

import org.example.backend_tunisiahub.Entities.Event.Event;

import java.util.List;

public record AiEventChatResponse(
        String message,
        List<Event> events
) {
}
