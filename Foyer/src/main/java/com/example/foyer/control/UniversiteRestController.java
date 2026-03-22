package com.example.foyer.control;

import com.example.foyer.entities.Universite;
import com.example.foyer.service.IUniversiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion Universite")
@RestController
@AllArgsConstructor
@RequestMapping("/universite")
public class UniversiteRestController {

    IUniversiteService universiteService;

    @Operation(description = "récupérer toutes les universités de la base de données")
    @GetMapping("/retrieve-all-universites")
    public List<Universite> getUniversites() {
        return universiteService.retrieveAllUniversites();
    }

    @GetMapping("/retrieve-universite/{id-universite}")
    public Universite retrieveUniversite(@PathVariable("id-universite") Long idUniversite) {
        return universiteService.retrieveUniversite(idUniversite);
    }

    @PostMapping("/add-universite")
    public Universite addUniversite(@RequestBody Universite universite) {
        return universiteService.addUniversite(universite);
    }

    @DeleteMapping("/remove-universite/{id-universite}")
    public void removeUniversite(@PathVariable("id-universite") Long idUniversite) {
        universiteService.removeUniversite(idUniversite);
    }

    @PutMapping("/modify-universite")
    public Universite modifyUniversite(@RequestBody Universite universite) {
        return universiteService.modifyUniversite(universite);
    }

    @PostMapping("/ajouter-universite-et-Foyer")
    public Universite addUniversiteAndFoyer(@RequestBody Universite u) {
        return universiteService.addUniversiteAndFoyerAndAssign(u);
    }

    @PutMapping("/affecter-universite-a-foyer/{universite-id}/{foyer-id}")
    public void affecterUniversiteAFoyer(
            @PathVariable("universite-id") Long universiteId,
            @PathVariable("foyer-id") Long foyerId) {
        universiteService.assignFoyerToUniversite(universiteId, foyerId);
    }

    @PostMapping("/creer-universite-et-affecter-foyer-a-universite/{foyer-id}")
    public Universite creerUniversiteEtAffecterFoyer(
            @RequestBody Universite universite,
            @PathVariable("foyer-id") Long foyerId) {
        return universiteService.addUniversiteAndAssignFoyerToUniversite(universite, foyerId);
    }

    @PutMapping("/desaffecter-foyer/{universite-id}")
    public Universite desaffecterFoyer(@PathVariable("universite-id") Long universiteId) {
        return universiteService.desaffecterFoyerFromUniversite(universiteId);
    }
}
