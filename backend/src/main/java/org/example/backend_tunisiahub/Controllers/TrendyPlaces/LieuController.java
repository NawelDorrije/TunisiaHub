package org.example.backend_tunisiahub.Controllers.TrendyPlaces;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;
import org.example.backend_tunisiahub.Services.TrendyPlaces.ILieuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lieux")
@RequiredArgsConstructor
public class LieuController {

    private final ILieuService lieuService;

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
}