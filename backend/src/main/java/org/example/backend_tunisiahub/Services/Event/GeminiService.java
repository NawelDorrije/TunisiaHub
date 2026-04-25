package org.example.backend_tunisiahub.Services.Event;

import org.springframework.web.multipart.MultipartFile;

public interface GeminiService {

    String generateEventDescription(MultipartFile imageFile, String imageUrl);
}
