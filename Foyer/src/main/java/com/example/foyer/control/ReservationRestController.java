package com.example.foyer.control;

import com.example.foyer.entities.Reservation;
import com.example.foyer.service.IReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Gestion Reservation")
@RestController
@AllArgsConstructor
@RequestMapping("/reservation")
public class ReservationRestController {

    IReservationService reservationService;

    @Operation(description = "récupérer toutes les réservations de la base de données")
    @GetMapping("/retrieve-all-reservations")
    public List<Reservation> getReservations() {
        return reservationService.retrieveAllReservations();
    }

    @GetMapping("/retrieve-reservation/{id-reservation}")
    public Reservation retrieveReservation(@PathVariable("id-reservation") Long idReservation) {
        return reservationService.retrieveReservation(idReservation);
    }

    @GetMapping("/retrieve-valid-reservations/{annee-universite}")
    public List<Reservation> retrieveValidReservationsByYear(@PathVariable("annee-universite") LocalDate anneUniversite) {
        return reservationService.retrieveValidReservationsByYear(anneUniversite);
    }

    @PostMapping("/add-reservation")
    public Reservation addReservation(@RequestBody Reservation reservation) {
        return reservationService.addReservation(reservation);
    }

    @DeleteMapping("/remove-reservation/{id-reservation}")
    public void removeReservation(@PathVariable("id-reservation") Long idReservation) {
        reservationService.removeReservation(idReservation);
    }

    @PutMapping("/modify-reservation")
    public Reservation modifyReservation(@RequestBody Reservation reservation) {
        return reservationService.modifyReservation(reservation);
    }
}
