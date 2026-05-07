package org.example.backend_tunisiahub.Controllers.dto;

public record ImageUploadAnalysisResponse(
        String imageUrl,
        String suggestedDescription
) {
}
