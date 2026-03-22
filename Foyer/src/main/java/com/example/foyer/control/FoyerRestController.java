package com.example.foyer.control;

import com.example.foyer.entities.Foyer;
import com.example.foyer.service.IFoyerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion Foyer")
@RestController
@AllArgsConstructor
@RequestMapping("/foyer")
public class FoyerRestController {

    IFoyerService foyerService;

    @Operation(description = "récupérer tous les foyers de la base de données")
    @GetMapping("/retrieve-all-foyers")
    public List<Foyer> getFoyers() {
        return foyerService.retrieveAllFoyers();
    }

    @GetMapping("/retrieve-foyer/{id-foyer}")
    public Foyer retrieveFoyer(@PathVariable("id-foyer") Long idFoyer) {
        return foyerService.retrieveFoyer(idFoyer);
    }

    @GetMapping("/retrieve-foyer-by-name/{nom-foyer}")
    public Foyer retrieveFoyerByName(@PathVariable("nom-foyer") String nomFoyer) {
        return foyerService.retrieveFoyerByName(nomFoyer);
    }

    @GetMapping("/retrieve-foyer-by-name-and-capacity/{nom-foyer}/{capacite}")
    public Foyer retrieveFoyerByNameAndCapacity(@PathVariable("nom-foyer") String nomFoyer,
                                                 @PathVariable("capacite") long capaciteFoyer) {
        return foyerService.retrieveFoyerByNameAndCapacity(nomFoyer, capaciteFoyer);
    }

    @PostMapping("/add-foyer")
    public Foyer addFoyer(@RequestBody Foyer foyer) {
        return foyerService.addFoyer(foyer);
    }

    @DeleteMapping("/remove-foyer/{id-foyer}")
    public void removeFoyer(@PathVariable("id-foyer") Long idFoyer) {
        foyerService.removeFoyer(idFoyer);
    }

    @PutMapping("/modify-foyer")
    public Foyer modifyFoyer(@RequestBody Foyer foyer) {
        return foyerService.modifyFoyer(foyer);
    }
}
