package org.example.backend_tunisiahub.Controllers.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Services.Accommodation.IAccommodationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
public class AccommodationController {

    private final IAccommodationService accommodationService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Accommodation>> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationService.retrieveAllAccommodations();
        return ResponseEntity.ok(accommodations);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getAccommodationById(@PathVariable Long id) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid accommodation ID");
        Accommodation accommodation = accommodationService.retrieveAccommodation(id);
        if (accommodation == null) return ResponseEntity.status(404).body("Accommodation not found with id: " + id);
        return ResponseEntity.ok(accommodation);
    }

    @PostMapping("/add")
    public ResponseEntity<?> createAccommodation(@RequestBody Accommodation accommodation) {
        if (accommodation.getTitle() == null || accommodation.getTitle().isEmpty())
            return ResponseEntity.badRequest().body("Title is required");
        if (accommodation.getPrice() <= 0)
            return ResponseEntity.badRequest().body("Price must be greater than 0");
        if (accommodation.getCapacite() <= 0)
            return ResponseEntity.badRequest().body("Capacity must be greater than 0");
        Accommodation saved = accommodationService.addAccommodation(accommodation);
        return ResponseEntity.status(201).body(saved);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAccommodation(@PathVariable Long id, @RequestBody Accommodation accommodation) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid accommodation ID");
        Accommodation existing = accommodationService.retrieveAccommodation(id);
        if (existing == null) return ResponseEntity.status(404).body("Accommodation not found with id: " + id);
        accommodation.setId(id);
        Accommodation updated = accommodationService.modifyAccommodation(accommodation);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Long id) {
        if (id <= 0) return ResponseEntity.badRequest().body("Invalid accommodation ID");
        Accommodation existing = accommodationService.retrieveAccommodation(id);
        if (existing == null) return ResponseEntity.status(404).body("Accommodation not found with id: " + id);
        accommodationService.removeAccommodation(id);
        return ResponseEntity.ok("Accommodation deleted successfully");
    }
}