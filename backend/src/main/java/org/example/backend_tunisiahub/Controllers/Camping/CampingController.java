package org.example.backend_tunisiahub.Controllers.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Services.Camping.FileStorageService;
import org.example.backend_tunisiahub.Services.Camping.ICampingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/campings")
@RequiredArgsConstructor
public class CampingController {

    private final ICampingService campingService;
    private final FileStorageService fileStorageService ;

    @GetMapping
    public List<Camping> getAllCampings() {
        return campingService.retrieveAllCampings();
    }

    @GetMapping("/{id}")
    public Camping getCampingById(@PathVariable Long id) {
        return campingService.retrieveCamping(id);
    }

    @PostMapping(consumes = "multipart/form-data")
    public Camping createCamping(

            @RequestPart("camping")
            Camping camping,

            @RequestPart("files")
            List<MultipartFile> files

    ) {

        List<String> photoUrls = new ArrayList<>();

        for (MultipartFile file : files) {

            String path =
                    fileStorageService.saveFile(file);

            photoUrls.add(path);

        }

        camping.setPhotos(photoUrls);

        return campingService.addCamping(camping);

    }

    @PutMapping(consumes = "multipart/form-data")
    public Camping updateCamping(

            @RequestPart("camping") Camping camping,

            @RequestPart(value = "files", required = false)
            List<MultipartFile> files

    ) {

        List<String> photoUrls = new ArrayList<>();

        // garder anciennes images si pas de nouvelles
        if (camping.getPhotos() != null) {
            photoUrls.addAll(camping.getPhotos());
        }

        if (files != null) {
            for (MultipartFile file : files) {

                String path = fileStorageService.saveFile(file);
                photoUrls.add(path);

            }
        }

        camping.setPhotos(photoUrls);

        return campingService.modifyCamping(camping);
    }
    @DeleteMapping("/{id}")
    public void deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
    }
}
