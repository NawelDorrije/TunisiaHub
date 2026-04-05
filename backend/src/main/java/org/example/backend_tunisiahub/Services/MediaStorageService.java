package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final Path uploadRoot;

    public MediaStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String storeRestaurantPicture(MultipartFile file) {
        return store(file, "restaurants");
    }

    public String storeMenuItemPicture(MultipartFile file) {
        return store(file, "menu-items");
    }

    public String storeRestaurantPicture(String picture) {
        return storeBase64Picture(picture, "restaurants");
    }

    public String storeMenuItemPicture(String picture) {
        return storeBase64Picture(picture, "menu-items");
    }

    private String store(MultipartFile file, String folder) {
        validateFile(file);

        return store(file.getOriginalFilename(), file.getContentType(), file.getSize(), folder, () -> file.getInputStream());
    }

    private String storeBase64Picture(String picture, String folder) {
        if (!StringUtils.hasText(picture)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image data is required");
        }

        String trimmed = picture.trim();
        if (!trimmed.startsWith("data:") || !trimmed.contains(";base64,")) {
            return trimmed;
        }

        int commaIndex = trimmed.indexOf(',');
        String metadata = trimmed.substring(5, commaIndex);
        String base64Data = trimmed.substring(commaIndex + 1);

        String contentType = metadata.substring(0, metadata.indexOf(';')).toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid image content type");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid base64 image data");
        }

        if (bytes.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file must be 5 MB or smaller");
        }

        String extension = contentTypeToExtension(contentType);
        String fileName = "upload" + extension;
        return store(fileName, contentType, bytes.length, folder, () -> new ByteArrayInputStream(bytes));
    }

    private String store(String originalFilename,
                         String contentType,
                         long size,
                         String folder,
                         InputStreamSupplier inputStreamSupplier) {
        validateMetadata(originalFilename, contentType, size);

        String extension = extractExtension(originalFilename);
        String fileName = UUID.randomUUID() + extension;
        Path targetDirectory = uploadRoot.resolve(folder).normalize();
        Path targetPath = targetDirectory.resolve(fileName).normalize();

        if (!targetPath.startsWith(targetDirectory)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = inputStreamSupplier.openStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store uploaded image");
        }

        return "/uploads/" + folder + "/" + fileName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        validateMetadata(file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    private void validateMetadata(String originalFilename, String contentType, long size) {
        if (size > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file must be 5 MB or smaller");
        }
        if (!StringUtils.hasText(originalFilename)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file name is required");
        }

        String extension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only jpg, jpeg, png, webp, and gif images are allowed");
        }

        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid image content type");
        }
    }

    private String extractExtension(String originalFilename) {
        String fileName = Paths.get(originalFilename).getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Image file must have an extension");
        }
        return fileName.substring(lastDot).toLowerCase();
    }

    private String contentTypeToExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid image content type");
        };
    }

    @FunctionalInterface
    private interface InputStreamSupplier {
        InputStream openStream() throws IOException;
    }
}
