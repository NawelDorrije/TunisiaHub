package org.example.backend_tunisiahub.Controllers.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;
import org.example.backend_tunisiahub.Services.TrendyPlaces.ILieuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lieux")
@RequiredArgsConstructor
public class LieuController {

    private final ILieuService lieuService;

    @Value("${app.upload.lieux-dir:../frontend/public/assets/images/lieux/}")
    private String uploadDir;

    @GetMapping
    public List<Lieu> getAllLieux() {
        return lieuService.retrieveAllLieux();
    }

    @GetMapping("/{id}")
    public Lieu getLieuById(@PathVariable Long id) {
        return lieuService.retrieveLieu(id);
    }

    @PostMapping
    public Lieu createLieu(@RequestBody Lieu lieu) {
        return lieuService.addLieu(lieu);
    }

    @PutMapping("/{id}")
    public Lieu updateLieu(@PathVariable Long id, @RequestBody Lieu lieu) {
        return lieuService.updateLieu(id, lieu);
    }

    @DeleteMapping("/{id}")
    public void deleteLieu(@PathVariable Long id) {
        lieuService.deleteLieu(id);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("📁 Dossier créé: " + dir.getAbsolutePath());
            }

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString().substring(0, 8) + extension;

            Path path = Paths.get(uploadDir).resolve(fileName);
            Files.write(path, file.getBytes());

            System.out.println("✅ Image sauvegardée: " + path.toAbsolutePath());

            // ← URL via Spring Boot (pas via Angular assets)
            String imageUrl = "http://localhost:8089/uploads/lieux/" + fileName;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "fileName", fileName,
                    "imageUrl", imageUrl  // ← URL complète
            ));

        } catch (IOException e) {
            System.err.println("❌ Erreur upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}