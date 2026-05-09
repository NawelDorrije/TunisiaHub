package org.example.backend_tunisiahub.Controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Services.IReservationRestaurantService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationRestaurantCheckInController {

    private final IReservationRestaurantService reservationService;

    @GetMapping(value = {"/checkin", "/checkin-public"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String checkInPublic(@RequestParam String token) {
        try {
            ReservationRestaurant reservation = reservationService.checkInReservation(token);
            String restaurantName = reservation.getRestaurant() != null && reservation.getRestaurant().getName() != null
                    ? escapeHtml(reservation.getRestaurant().getName())
                    : "the restaurant";
            return """
                    <!DOCTY02PE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Check-in Successful</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 0; background: #f5f1e8; color: #1f2937; }
                            main { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
                            section { background: white; width: min(520px, 100%%); border-radius: 16px; padding: 32px; box-shadow: 0 18px 50px rgba(0,0,0,.12); text-align: center; }
                            h1 { margin: 0 0 16px; color: #166534; font-size: 2rem; }
                            p { margin: 10px 0; line-height: 1.5; }
                            .pill { display: inline-block; margin-top: 12px; padding: 8px 14px; border-radius: 999px; background: #dcfce7; color: #166534; font-weight: 700; }
                        </style>
                    </head>
                    <body>
                    <main>
                        <section>
                            <h1>Check-in Successful</h1>
                            <p>Welcome to <b>%s</b></p>
                            <p>Your reservation has been validated successfully.</p>
                            <span class="pill">Status: ARRIVED</span>
                        </section>
                    </main>
                    </body>
                    </html>
                    """.formatted(restaurantName);
        } catch (Exception ex) {
            return """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>Check-in Failed</title>
                        <style>
                            body { font-family: Arial, sans-serif; margin: 0; background: #fff1f2; color: #1f2937; }
                            main { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
                            section { background: white; width: min(520px, 100%%); border-radius: 16px; padding: 32px; box-shadow: 0 18px 50px rgba(0,0,0,.12); text-align: center; }
                            h1 { margin: 0 0 16px; color: #b91c1c; font-size: 2rem; }
                            p { margin: 10px 0; line-height: 1.5; }
                        </style>
                    </head>
                    <body>
                    <main>
                        <section>
                            <h1>Check-in Failed</h1>
                            <p>%s</p>
                        </section>
                    </main>
                    </body>
                    </html>
                    """.formatted(escapeHtml(ex.getMessage()));
        }
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
