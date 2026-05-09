package org.example.backend_tunisiahub.dto.event;

public record EventImageUploadResponse(
        String imageUrl,
        String aiDescription
) {
}
