package org.example.backend_tunisiahub.Services.Event;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageUploadService {

    String uploadEventImage(MultipartFile file) throws IOException;
}
