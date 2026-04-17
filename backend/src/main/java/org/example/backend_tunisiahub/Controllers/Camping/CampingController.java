package org.example.backend_tunisiahub.Controllers.Camping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.CampingDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;
import org.example.backend_tunisiahub.Services.Camping.IActivityService;
import org.example.backend_tunisiahub.Services.Camping.ICampingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/campings")
public class CampingController {

    @Autowired private ICampingService campingService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private IActivityService  activityService;

    // ── CRUD ───────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CampingDTO> createCamping(
            @RequestPart("camping") String campingJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
        try {
            CampingDTO dto = objectMapper.readValue(campingJson, CampingDTO.class);
            return ResponseEntity.ok(campingService.createCamping(dto, photos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CampingDTO> updateCamping(
            @PathVariable Long id,
            @RequestPart("camping") String campingJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> newPhotos) {
        try {
            CampingDTO dto = objectMapper.readValue(campingJson, CampingDTO.class);
            return ResponseEntity.ok(campingService.updateCamping(id, dto, newPhotos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CampingDTO>> getAllCampings() {
        return ResponseEntity.ok(campingService.getAllCampings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampingDTO> getCampingById(@PathVariable Long id) {
        return campingService.getCampingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── FILTERS — CLIENT ───────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<CampingDTO>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(campingService.searchByKeyword(keyword));
    }

    @GetMapping("/available")
    public ResponseEntity<List<CampingDTO>> getAvailable(
            @RequestParam(required = false) String governorate,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minCapacity) {
        return ResponseEntity.ok(campingService.getAvailableByFilters(governorate, maxPrice, minCapacity));
    }

    @GetMapping("/available-for-dates")
    public ResponseEntity<List<CampingDTO>> getAvailableForDates(
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut,
            @RequestParam(required = false) String governorate) {
        return ResponseEntity.ok(campingService.getWithAvailableSpotsForDates(checkIn, checkOut, governorate));
    }

    @GetMapping("/rating")
    public ResponseEntity<List<CampingDTO>> getByRating(
            @RequestParam BigDecimal minRating) {
        return ResponseEntity.ok(campingService.getByMinRating(minRating));
    }

    // ── FILTERS — OWNER ────────────────────────────────────

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<CampingDTO>> getByOwner(
            @PathVariable Long ownerId) {
        return ResponseEntity.ok(campingService.getByOwner(ownerId));
    }

    // ── FILTERS — ADMIN ────────────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<List<CampingDTO>> getByStatus(
            @RequestParam CampingStatus status) {
        return ResponseEntity.ok(campingService.getByStatus(status));
    }

    @GetMapping("/governorate")
    public ResponseEntity<List<CampingDTO>> getByGovernorate(
            @RequestParam String governorate) {
        return ResponseEntity.ok(campingService.getByGovernorate(governorate));
    }
    @PutMapping("/campings/{campingId}/assign-activity/{activityId}")
    public ResponseEntity<ActivityDTO> assignActivityToCamping(
            @PathVariable Long campingId,
            @PathVariable Long activityId
    ) {

        return ResponseEntity.ok(
                activityService.assignActivity(
                        activityId,
                        campingId,
                        null
                )
        );
    }

}