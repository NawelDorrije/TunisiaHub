package org.example.backend_tunisiahub.dto.event;

import jakarta.validation.constraints.NotBlank;

public record AiEventChatRequest(
        @NotBlank(message = "Message is required")
        String message
) {
}
