package org.example.backend_tunisiahub.Services.Event;

import org.springframework.web.multipart.MultipartFile;
import org.example.backend_tunisiahub.Entities.Event.Event;

public interface GeminiService {

    String generateEventDescription(MultipartFile imageFile, String imageUrl);
    String generateMarketingDescription(Event event);
    String generatePosterImage(Event event);
}
