package org.example.backend_tunisiahub.Controllers.Camping;

import jakarta.validation.Valid;
import org.example.backend_tunisiahub.Entities.Camping.DTO.EquipementDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;
import org.example.backend_tunisiahub.Services.Camping.IEquipementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipements")
public class EquipementController {

    private final IEquipementService equipementService;

    public EquipementController(IEquipementService equipementService) {
        this.equipementService = equipementService;
    }

    @PostMapping
    public ResponseEntity<EquipementDTO> create(@Valid @RequestBody EquipementDTO dto) {
        return ResponseEntity.ok(equipementService.createEquipement(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipementDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipementDTO dto) {
        return ResponseEntity.ok(equipementService.updateEquipement(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipementService.deleteEquipement(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipementDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipementService.getEquipementById(id));
    }

    @GetMapping
    public ResponseEntity<List<EquipementDTO>> getAll() {
        return ResponseEntity.ok(equipementService.getAllEquipements());
    }

    // ── Filters ────────────────────────────────────────────

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<List<EquipementDTO>> getBySpot(@PathVariable Long spotId) {
        return ResponseEntity.ok(equipementService.getBySpotId(spotId));
    }

    @GetMapping("/available")
    public ResponseEntity<List<EquipementDTO>> getByAvailability(
            @RequestParam Boolean available) {
        return ResponseEntity.ok(equipementService.getByAvailability(available));
    }

    @GetMapping("/condition")
    public ResponseEntity<List<EquipementDTO>> getByCondition(
            @RequestParam EquipmentCondition condition) {
        return ResponseEntity.ok(equipementService.getByCondition(condition));
    }

    @GetMapping("/spot/{spotId}/available")
    public ResponseEntity<List<EquipementDTO>> getBySpotAndAvailability(
            @PathVariable Long spotId,
            @RequestParam Boolean available) {
        return ResponseEntity.ok(equipementService.getBySpotAndAvailability(spotId, available));
    }

    @GetMapping("/spot/{spotId}/condition")
    public ResponseEntity<List<EquipementDTO>> getBySpotAndCondition(
            @PathVariable Long spotId,
            @RequestParam EquipmentCondition condition) {
        return ResponseEntity.ok(equipementService.getBySpotAndCondition(spotId, condition));
    }
    @GetMapping("/camping/{campingId}")
    public ResponseEntity<List<EquipementDTO>> getByCamping(@PathVariable Long campingId) {
        return ResponseEntity.ok(equipementService.getByCampingId(campingId));
    }
}