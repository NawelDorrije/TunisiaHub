package org.example.backend_tunisiahub.Controllers;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@CrossOrigin("*")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReservationRepository reservationRepository;

    @GetMapping("/send")
    public String sendEmail(@RequestParam Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);

        if (reservation == null) {
            return "Reservation not found";
        }

        String email = reservation.getUser().getEmail();
        String name = reservation.getUser().getNom();
        double amount = reservation.getTotalPrice();

        emailService.sendPaymentEmail(email, name, amount);

        return "Email sent successfully";
    }
}