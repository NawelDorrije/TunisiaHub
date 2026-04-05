package org.example.backend_tunisiahub.Controllers.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Services.MediaStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class MediaUploadController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(value = "/restaurants-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadRestaurantPicture(@RequestParam("file") MultipartFile file) {
        return new UploadResponse(mediaStorageService.storeRestaurantPicture(file));
    }

    @PostMapping(value = "/menu-items-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse uploadMenuItemPicture(@RequestParam("file") MultipartFile file) {
        return new UploadResponse(mediaStorageService.storeMenuItemPicture(file));
    }

    private record UploadResponse(String picture) {}
}
