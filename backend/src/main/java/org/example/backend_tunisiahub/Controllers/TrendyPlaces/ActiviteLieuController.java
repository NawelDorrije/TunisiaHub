package org.example.backend_tunisiahub.Controllers.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.ActiviteLieu;
import org.example.backend_tunisiahub.Services.TrendyPlaces.IActiviteLieuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activites")
@RequiredArgsConstructor
public class ActiviteLieuController {

    private final IActiviteLieuService activiteLieuService;

    @GetMapping
    public List<ActiviteLieu> getAllActivites() {
        return activiteLieuService.retrieveAllActivites();
    }

    @GetMapping("/lieu/{lieuId}")
    public List<ActiviteLieu> getActivitesByLieu(@PathVariable Long lieuId) {
        return activiteLieuService.retrieveActivitesByLieu(lieuId);
    }

    @GetMapping("/{id}")
    public ActiviteLieu getActiviteById(@PathVariable Long id) {
        return activiteLieuService.retrieveActivite(id);
    }

    @PostMapping("/lieu/{lieuId}")
    public ActiviteLieu createActivite(@PathVariable Long lieuId,
                                       @RequestBody ActiviteLieu activite) {
        return activiteLieuService.addActivite(activite, lieuId);
    }

    @PutMapping("/{id}/lieu/{lieuId}")
    public ActiviteLieu updateActivite(@PathVariable Long id,
                                       @PathVariable Long lieuId,
                                       @RequestBody ActiviteLieu activite) {
        return activiteLieuService.updateActivite(id, activite, lieuId);
    }

    @DeleteMapping("/{id}")
    public void deleteActivite(@PathVariable Long id) {
        activiteLieuService.deleteActivite(id);
    }
}