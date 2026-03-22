package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Services.IReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final IReservationService reservationService;

    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.retrieveAllReservations();
    }

    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id) {
        return reservationService.retrieveReservation(id);
    }

    @PostMapping
    public Reservation createReservation(
            @RequestBody ReservationDTO dto
    ) {
        return reservationService.addReservationCamping(dto);
    }

    @PutMapping
    public Reservation updateReservation(@RequestBody Reservation reservation) {
        return reservationService.modifyReservation(reservation);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }
}
