package com.example.foyer.control;

import com.example.foyer.entities.Chambre;
import com.example.foyer.entities.TypeChambre;
import com.example.foyer.service.IChambreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
// http://localhost:8089/foyer/swagger-ui/index.html
import java.util.List;

@Tag(name = "Gestion Chambre")
@RestController
@AllArgsConstructor
@RequestMapping("/chambre")
public class ChambreRestController {

    IChambreService chambreService;

    // http://localhost:8089/tpfoyer/chambre/retrieve-all-chambres
    @Operation(description = "récupérer toutes les chambres de la base de données")
    @GetMapping("/retrieve-all-chambres")
    public List<Chambre> getChambres() {
        return chambreService.retrieveAllChambres();}
    /*
    // http://localhost:8089/foyer/chambre/retrieve-all-chambres
    @GetMapping("/retrieve-all-chambres")
    public List<Chambre> getChambres() {
        List<Chambre> listchambres= chambreService.retrieveAllChambres();
        return listchambres;
    }
*/
    // http://localhost:8089/foyer/chambre/retrieve-chambre/8
    @GetMapping("/retrieve-chambre/{chambre-id}")
    public Chambre retrieveChambre(@PathVariable("chambre-id") Long chId) {
        return chambreService.retrieveChambre(chId);
    }

    // http://localhost:8089/foyer/chambre/add-chambre
    @PostMapping("/add-chambre")
    public Chambre addChambre(@RequestBody Chambre c) {
        return chambreService.addChambre(c);
    }

    // http://localhost:8089/foyer/chambre/remove-chambre/8
    @DeleteMapping("/remove-chambre/{chambre-id}")
    public void removeChambre(@PathVariable("chambre-id") Long chId) {
        chambreService.removeChambre(chId);
    }
//     public void removeChambre(@requestparam Long chId) {
//        chambreService.removeChambre(chId);
//    }
    // http://localhost:8089/foyer/chambre/modify-chambre
    @PutMapping("/modify-chambre")
    public Chambre modifyChambre(@RequestBody Chambre c) {
        return chambreService.modifyChambre(c);
    }
    // http://localhost:8089/foyer/chambre/type/{type}/numero/{numero}

    @GetMapping("/type/{type}/numero/{numero}")
    public List<Chambre> retrieveChambresByTypeAndNumero(
            @PathVariable("type") TypeChambre type,
            @PathVariable("numero") Long numeroChambre) {
        return chambreService.retrieveChambresByTypeAndNumero(type, numeroChambre);
    }
}
