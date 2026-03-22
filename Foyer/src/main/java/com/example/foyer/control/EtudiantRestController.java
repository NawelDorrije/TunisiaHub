package com.example.foyer.control;

import com.example.foyer.entities.Etudiant;
import com.example.foyer.service.IEtudiantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Gestion Etudiant")
@RestController
@AllArgsConstructor
@RequestMapping("/etudiant")
public class EtudiantRestController {

    IEtudiantService etudiantService;

    @Operation(description = "récupérer tous les étudiants de la base de données")
    @GetMapping("/retrieve-all-etudiants")
    public List<Etudiant> getEtudiants() {
        return etudiantService.retrieveAllEtudiants();
    }

    @GetMapping("/retrieve-etudiant/{id-etudiant}")
    public Etudiant retrieveEtudiant(@PathVariable("id-etudiant") Long idEtudiant) {
        return etudiantService.retrieveEtudiant(idEtudiant);
    }

    @PostMapping("/add-etudiant")
    public Etudiant addEtudiant(@RequestBody Etudiant etudiant) {
        return etudiantService.addEtudiant(etudiant);
    }

    @DeleteMapping("/remove-etudiant/{id-etudiant}")
    public void removeEtudiant(@PathVariable("id-etudiant") Long idEtudiant) {
        etudiantService.removeEtudiant(idEtudiant);
    }

    @PutMapping("/modify-etudiant")
    public Etudiant modifyEtudiant(@RequestBody Etudiant etudiant) {
        return etudiantService.modifyEtudiant(etudiant);
    }

    @GetMapping("/count/{date-naissance}/{ecole}")
    public long countStudentsByBirthDateAndSchool(@PathVariable("date-naissance") LocalDate dateNaissance,
                                                    @PathVariable("ecole") String ecole) {
        return etudiantService.countStudentsByBirthDateAndSchool(dateNaissance, ecole);
    }

    @PutMapping("/affecter-etudiant-reservation/{id-etudiant}/{id-reservation}")
    public void affecterEtudiantAReservation(
            @PathVariable("id-etudiant") Long idEtudiant,
            @PathVariable("id-reservation") Long idReservation) {
        etudiantService.assignEtudiantToReservation(idEtudiant, idReservation);
    }

    @PutMapping("/desaffecter-etudiant-de-reservation/{id-etudiant}/{id-reservation}")
    public void desaffecterEtudiantDeReservation(
            @PathVariable("id-etudiant") Long idEtudiant,
            @PathVariable("id-reservation") Long idReservation) {
        etudiantService.desaffecterEtudiantFromReservation(idEtudiant, idReservation);
    }
}
