package org.example.backend_tunisiahub.Controllers.Camping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;
import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;
import org.example.backend_tunisiahub.Services.Camping.IActivityService;
import org.example.backend_tunisiahub.Services.Camping.ISpotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/spots")
public class SpotController {

  @Autowired private ISpotService spotService;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private IActivityService activityService;

  // ── CRUD ───────────────────────────────────────────────

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SpotDTO> createSpot(
    @RequestPart("spot") String spotJson,
    @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {
    try {
      SpotDTO dto = objectMapper.readValue(spotJson, SpotDTO.class);
      return ResponseEntity.ok(spotService.createSpot(dto, photos));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SpotDTO> updateSpot(
    @PathVariable Long id,
    @RequestPart("spot") String spotJson,
    @RequestPart(value = "photos", required = false) List<MultipartFile> newPhotos) {
    try {
      SpotDTO dto = objectMapper.readValue(spotJson, SpotDTO.class);
      return ResponseEntity.ok(spotService.updateSpot(id, dto, newPhotos));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSpot(@PathVariable Long id) {
    spotService.deleteSpot(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<SpotDTO>> getAllSpots() {
    return ResponseEntity.ok(spotService.getAllSpots());
  }

  @GetMapping("/{id}")
  public ResponseEntity<SpotDTO> getSpotById(@PathVariable Long id) {
    return spotService.getSpotById(id)
      .map(ResponseEntity::ok)
      .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/camping/{campingId}")
  public ResponseEntity<List<SpotDTO>> getSpotsByCamping(@PathVariable Long campingId) {
    return ResponseEntity.ok(spotService.getSpotsByCampingId(campingId));
  }

  // ── FILTERS — CLIENT ───────────────────────────────────

  @GetMapping("/camping/{campingId}/available")
  public ResponseEntity<List<SpotDTO>> getAvailableByDates(
    @PathVariable Long campingId,
    @RequestParam LocalDate checkIn,
    @RequestParam LocalDate checkOut) {
    return ResponseEntity.ok(spotService.getAvailableByDates(campingId, checkIn, checkOut));
  }

  @GetMapping("/camping/{campingId}/search")
  public ResponseEntity<List<SpotDTO>> searchSpots(
    @PathVariable Long campingId,
    @RequestParam LocalDate checkIn,
    @RequestParam LocalDate checkOut,
    @RequestParam(required = false) SpotType type,
    @RequestParam(required = false) ViewType viewType,
    @RequestParam(required = false) Boolean hasShade,
    @RequestParam(required = false) Boolean accessibleForDisabled,
    @RequestParam(required = false) Integer minCapacity,
    @RequestParam(required = false) BigDecimal maxPrice) {
    return ResponseEntity.ok(spotService.getAvailableWithFilters(
      campingId, checkIn, checkOut,
      type, viewType, hasShade,
      accessibleForDisabled, minCapacity, maxPrice));
  }

  @GetMapping("/camping/{campingId}/price-range")
  public ResponseEntity<List<SpotDTO>> getByPriceRange(
    @PathVariable Long campingId,
    @RequestParam BigDecimal min,
    @RequestParam BigDecimal max) {
    return ResponseEntity.ok(spotService.getByPriceRange(campingId, min, max));
  }

  @GetMapping("/camping/{campingId}/capacity")
  public ResponseEntity<List<SpotDTO>> getByMinCapacity(
    @PathVariable Long campingId,
    @RequestParam Integer minCapacity) {
    return ResponseEntity.ok(spotService.getByMinCapacity(campingId, minCapacity));
  }

  @GetMapping("/camping/{campingId}/view")
  public ResponseEntity<List<SpotDTO>> getByViewType(
    @PathVariable Long campingId,
    @RequestParam ViewType viewType) {
    return ResponseEntity.ok(spotService.getByViewType(campingId, viewType));
  }

  @GetMapping("/camping/{campingId}/shade")
  public ResponseEntity<List<SpotDTO>> getByShade(
    @PathVariable Long campingId,
    @RequestParam Boolean hasShade) {
    return ResponseEntity.ok(spotService.getByShade(campingId, hasShade));
  }

  @GetMapping("/camping/{campingId}/accessible")
  public ResponseEntity<List<SpotDTO>> getByAccessibility(
    @PathVariable Long campingId,
    @RequestParam Boolean accessibleForDisabled) {
    return ResponseEntity.ok(spotService.getByAccessibility(campingId, accessibleForDisabled));
  }

  // ── FILTERS — OWNER ────────────────────────────────────

  @GetMapping("/camping/{campingId}/status")
  public ResponseEntity<List<SpotDTO>> getByStatus(
    @PathVariable Long campingId,
    @RequestParam SpotStatus status) {
    return ResponseEntity.ok(spotService.getByStatus(campingId, status));
  }

  @GetMapping("/camping/{campingId}/type")
  public ResponseEntity<List<SpotDTO>> getByType(
    @PathVariable Long campingId,
    @RequestParam SpotType type) {
    return ResponseEntity.ok(spotService.getByType(campingId, type));
  }
  @PutMapping("/spots/{spotId}/assign-activity/{activityId}")
  public ResponseEntity<ActivityDTO> assignActivityToSpot(
    @PathVariable Long spotId,
    @PathVariable Long activityId
  ) {

    return ResponseEntity.ok(
      activityService.assignActivity(
        activityId,
        null,
        spotId
      )
    );
  }
}
