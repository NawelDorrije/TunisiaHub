package org.example.backend_tunisiahub.Services.Camping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) {

        try {

            Path uploadPath =
                    Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {

                Files.createDirectories(
                        uploadPath
                );

            }

            String fileName =
                    UUID.randomUUID()
                            + "_"
                            + file.getOriginalFilename();

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return uploadDir + "/" + fileName;

        }

        catch (IOException e) {

            throw new RuntimeException(
                    "Could not save file",
                    e
            );

        }

    }

}