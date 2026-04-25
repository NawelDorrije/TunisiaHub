package org.example.backend_tunisiahub.Services.Event;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class CloudinaryImageUploadService implements ImageUploadService {

    private final Cloudinary cloudinaryClient;
    private final Path localUploadsPath;
    private final String publicBaseUrl;

    public CloudinaryImageUploadService(
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            @Value("${app.upload.local-dir:uploads}") String localUploadDir,
            @Value("${app.public-base-url:http://localhost:8089}") String publicBaseUrl
    ) {
        this.publicBaseUrl = publicBaseUrl;
        this.localUploadsPath = Paths.get(localUploadDir, "events").toAbsolutePath().normalize();

        if (hasText(cloudName) && hasText(apiKey) && hasText(apiSecret)) {
            this.cloudinaryClient = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
        } else {
            this.cloudinaryClient = null;
            log.info("Cloudinary credentials not provided. Local upload fallback enabled.");
        }
    }

    @Override
    public String uploadEventImage(MultipartFile file) throws IOException {
        if (cloudinaryClient != null) {
            try {
                return uploadToCloudinary(file);
            } catch (Exception ex) {
                log.warn("Cloudinary upload failed. Falling back to local storage.", ex);
            }
        }

        return uploadToLocal(file);
    }

    private String uploadToCloudinary(MultipartFile file) throws IOException {
        Map<?, ?> result = cloudinaryClient.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "events",
                "resource_type", "image"
        ));

        Object secureUrl = result.get("secure_url");
        if (secureUrl == null) {
            throw new IOException("Cloudinary response does not contain secure_url.");
        }

        return secureUrl.toString();
    }

    private String uploadToLocal(MultipartFile file) throws IOException {
        Files.createDirectories(localUploadsPath);
        String extension = resolveFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        Path targetFile = localUploadsPath.resolve(fileName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return stripTrailingSlash(publicBaseUrl) + "/uploads/events/" + fileName;
    }

    private String resolveFileExtension(String originalFileName) {
        if (!hasText(originalFileName)) {
            return ".jpg";
        }

        int extensionSeparatorIndex = originalFileName.lastIndexOf('.');
        if (extensionSeparatorIndex < 0 || extensionSeparatorIndex == originalFileName.length() - 1) {
            return ".jpg";
        }

        return originalFileName.substring(extensionSeparatorIndex);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String stripTrailingSlash(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return "http://localhost:8089";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
