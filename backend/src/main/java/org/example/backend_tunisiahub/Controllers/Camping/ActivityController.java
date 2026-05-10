package org.example.backend_tunisiahub.Controllers.Camping;

import jakarta.validation.Valid;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;
import org.example.backend_tunisiahub.Services.Camping.IActivityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final IActivityService activityService;

    public ActivityController(IActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody ActivityDTO dto) {
        return ResponseEntity.ok(activityService.createActivity(dto));
    }
    @PostMapping("/template")
    public ResponseEntity<ActivityDTO> createActivityTemplate(
            @Valid @RequestBody ActivityDTO dto
    ) {
        return ResponseEntity.ok(
                activityService.createActivityTemplate(dto)
        );
    }
    @PutMapping("/{id}")
    public ResponseEntity<ActivityDTO> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityDTO dto) {
        return ResponseEntity.ok(activityService.updateActivity(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityDTO> getActivityById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityById(id));
    }

    @GetMapping
    public ResponseEntity<List<ActivityDTO>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/camping/{campingId}")
    public ResponseEntity<List<ActivityDTO>> getActivitiesByCamping(@PathVariable Long campingId) {
        return ResponseEntity.ok(activityService.getActivitiesByCampingId(campingId));
    }

    @GetMapping("/spot/{spotId}")
    public ResponseEntity<List<ActivityDTO>> getActivitiesBySpot(@PathVariable Long spotId) {
        return ResponseEntity.ok(activityService.getActivitiesBySpotId(spotId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{activityId}/assign")
    public ResponseEntity<ActivityDTO> assignActivity(
            @PathVariable Long activityId,
            @RequestParam(required = false) Long campingId,
            @RequestParam(required = false) Long spotId
    ) {

        return ResponseEntity.ok(
                activityService.assignActivity(
                        activityId,
                        campingId,
                        spotId
                )
        );
    }
}