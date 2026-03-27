package org.example.backend_tunisiahub.Controllers.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Services.Camping.ICampingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campings")
@RequiredArgsConstructor
public class CampingController {

    private final ICampingService campingService;

    @GetMapping
    public List<Camping> getAllCampings() {
        return campingService.retrieveAllCampings();
    }

    @GetMapping("/{id}")
    public Camping getCampingById(@PathVariable Long id) {
        return campingService.retrieveCamping(id);
    }

    @PostMapping
    public Camping createCamping(@RequestBody Camping camping) {
        return campingService.addCamping(camping);
    }

    @PutMapping
    public Camping updateCamping(@RequestBody Camping camping) {
        return campingService.modifyCamping(camping);
    }

    @DeleteMapping("/{id}")
    public void deleteCamping(@PathVariable Long id) {
        campingService.deleteCamping(id);
    }

}
