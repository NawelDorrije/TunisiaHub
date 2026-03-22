package com.example.foyer.control;

import com.example.foyer.entities.Bloc;
import com.example.foyer.service.IBlocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gestion Bloc")
@RestController
@AllArgsConstructor
@RequestMapping("/bloc")
public class BlocRestController {

    IBlocService blocService;

    @Operation(description = "récupérer tous les blocs de la base de données")
    @GetMapping("/retrieve-all-blocs")
    public List<Bloc> getBlocs() {
        return blocService.retrieveAllBlocs();
    }

    @GetMapping("/retrieve-bloc/{id-bloc}")
    public Bloc retrieveBloc(@PathVariable("id-bloc") Long idBloc) {
        return blocService.retrieveBloc(idBloc);
    }

    @PostMapping("/add-bloc")
    public Bloc addBloc(@RequestBody Bloc bloc) {
        return blocService.addBloc(bloc);
    }

    @DeleteMapping("/remove-bloc/{id-bloc}")
    public void removeBloc(@PathVariable("id-bloc") Long idBloc) {
        blocService.removeBloc(idBloc);
    }

    @PutMapping("/modify-bloc")
    public Bloc modifyBloc(@RequestBody Bloc bloc) {
        return blocService.modifyBloc(bloc);
    }
}
