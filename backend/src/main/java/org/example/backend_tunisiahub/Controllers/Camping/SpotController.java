package org.example.backend_tunisiahub.Controllers.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Services.Camping.ISpotService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spots")
@RequiredArgsConstructor
public class SpotController {

    private final ISpotService spotService;

    @GetMapping
    public List<Spot> getAllSpots() {
        return spotService.retrieveAllSpots();
    }

    @GetMapping("/{id}")
    public Spot getSpotById(@PathVariable Long id) {
        return spotService.retrieveSpot(id);
    }

    @PostMapping
    public Spot createSpot(@RequestBody Spot spot) {
        return spotService.addSpot(spot);
    }

    @PutMapping
    public Spot updateSpot(@RequestBody Spot spot) {
        return spotService.modifySpot(spot);
    }

    @DeleteMapping("/{id}")
    public void deleteSpot(@PathVariable Long id) {
        spotService.deleteSpot(id);
    }
}
