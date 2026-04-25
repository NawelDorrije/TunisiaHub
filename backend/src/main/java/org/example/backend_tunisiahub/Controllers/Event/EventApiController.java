package org.example.backend_tunisiahub.Controllers.Event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Services.Event.GeminiService;
import org.example.backend_tunisiahub.Services.Event.IEventService;
import org.example.backend_tunisiahub.Services.Event.ImageUploadService;
import org.example.backend_tunisiahub.dto.event.EventImageUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventApiController {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

    private final IEventService eventService;
    private final ImageUploadService imageUploadService;
    private final GeminiService geminiService;

    @PostMapping(path = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventImageUploadResponse> uploadImage(@RequestParam("image") MultipartFile image) {
        validateImage(image);

        try {
            String imageUrl = imageUploadService.uploadEventImage(image);
            String aiDescription = geminiService.generateEventDescription(image, imageUrl);
            return ResponseEntity.ok(new EventImageUploadResponse(imageUrl, aiDescription));
        } catch (Exception ex) {
            log.error("Failed to upload event image and generate AI description.", ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed.");
        }
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.addEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required.");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed.");
        }

        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must not exceed 5 MB.");
        }
    }
}
