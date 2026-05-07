package org.example.backend_tunisiahub.Services.Accommodation.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Services.Accommodation.Location.NearbyPlaceService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationEmailService {

    private final JavaMailSender mailSender;
    private final NearbyPlaceService nearbyPlaceService;

    public void sendAccommodationBookingConfirmation(User user, Accommodation accommodation, Reservation reservation) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        NearbyPlaceService.PlaceInfo nearestHospital = nearbyPlaceService
                .findNearestHospital(accommodation.getLatitude(), accommodation.getLongitude());
        NearbyPlaceService.PlaceInfo nearestMarket = nearbyPlaceService
                .findNearestMarket(accommodation.getLatitude(), accommodation.getLongitude());

        String fullName = ((user.getPrenom() != null ? user.getPrenom() : "") + " "
                + (user.getNom() != null ? user.getNom() : "")).trim();
        if (fullName.isEmpty()) {
            fullName = "Traveler";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                String reservationDates = sdf.format(reservation.getStartDate()) + " to " + sdf.format(reservation.getEndDate());
                String totalPrice = (reservation.getTotalPrice() != null ? reservation.getTotalPrice() : 0) + " TND";

                String hospitalLinks = buildMapLinks(nearestHospital);
                String marketLinks = buildMapLinks(nearestMarket);

                String htmlBodyTemplate = """
                                <html>
                                    <body style=\"margin:0;padding:0;background:#f6f8fb;font-family:Segoe UI,Arial,sans-serif;color:#1a2233;\">
                                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">
                                            <tr>
                                                <td align=\"center\">
                                                    <table width=\"680\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:680px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e7ebf3;\">
                                                        <tr>
                                                            <td style=\"background:#0f4c81;color:#ffffff;padding:20px 24px;\">
                                                                <h2 style=\"margin:0;font-size:24px;\">TunisiaHub Booking Confirmation</h2>
                                                                <p style=\"margin:8px 0 0 0;font-size:14px;opacity:.92;\">Your accommodation reservation is confirmed.</p>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td style=\"padding:22px 24px;\">
                                                                <p style=\"margin:0 0 14px 0;font-size:15px;\">Hello <strong>{{FULL_NAME}}</strong>,</p>

                                                                <table width=\"100%\" cellspacing=\"0\" cellpadding=\"8\" style=\"border:1px solid #ecf0f7;border-radius:10px;background:#fbfcff;\">
                                                                    <tr><td><strong>Accommodation:</strong> {{ACCOMMODATION_TITLE}}</td></tr>
                                                                    <tr><td><strong>Address:</strong> {{ACCOMMODATION_ADDRESS}}</td></tr>
                                                                    <tr><td><strong>Type:</strong> {{ACCOMMODATION_TYPE}}</td></tr>
                                                                    <tr><td><strong>Dates:</strong> {{RESERVATION_DATES}}</td></tr>
                                                                    <tr><td><strong>Total price:</strong> {{TOTAL_PRICE}}</td></tr>
                                                                </table>

                                                                <h3 style=\"margin:22px 0 10px 0;font-size:18px;color:#0f4c81;\">Nearby essential places</h3>

                                                                <table width=\"100%\" cellspacing=\"0\" cellpadding=\"10\" style=\"border-collapse:separate;border-spacing:0 8px;\">
                                                                    <tr style=\"background:#f7faff;\">
                                                                        <td style=\"border:1px solid #e7eef9;border-radius:8px;\">
                                                                            <div style=\"font-size:14px;\"><strong>Nearest hospital:</strong> {{HOSPITAL_NAME}} ({{HOSPITAL_DISTANCE}})</div>
                                                                            <div style=\"font-size:13px;color:#42516c;margin-top:6px;\">Coordinates: {{HOSPITAL_COORDS}}</div>
                                                                            <div style=\"margin-top:8px;font-size:13px;\">{{HOSPITAL_LINKS}}</div>
                                                                        </td>
                                                                    </tr>
                                                                    <tr style=\"background:#f7faff;\">
                                                                        <td style=\"border:1px solid #e7eef9;border-radius:8px;\">
                                                                            <div style=\"font-size:14px;\"><strong>Nearest market:</strong> {{MARKET_NAME}} ({{MARKET_DISTANCE}})</div>
                                                                            <div style=\"font-size:13px;color:#42516c;margin-top:6px;\">Coordinates: {{MARKET_COORDS}}</div>
                                                                            <div style=\"margin-top:8px;font-size:13px;\">{{MARKET_LINKS}}</div>
                                                                        </td>
                                                                    </tr>
                                                                </table>

                                                                <p style=\"margin:16px 0 0 0;font-size:14px;color:#495770;\">Thank you for choosing TunisiaHub.</p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </body>
                                </html>
                                """;

                String htmlBody = htmlBodyTemplate
                                .replace("{{FULL_NAME}}", escapeHtml(fullName))
                                .replace("{{ACCOMMODATION_TITLE}}", escapeHtml(accommodation.getTitle()))
                                .replace("{{ACCOMMODATION_ADDRESS}}", escapeHtml(accommodation.getAdresse()))
                                .replace("{{ACCOMMODATION_TYPE}}", escapeHtml(accommodation.getType()))
                                .replace("{{RESERVATION_DATES}}", escapeHtml(reservationDates))
                                .replace("{{TOTAL_PRICE}}", escapeHtml(totalPrice))
                                .replace("{{HOSPITAL_NAME}}", escapeHtml(nearestHospital.name()))
                                .replace("{{HOSPITAL_DISTANCE}}", escapeHtml(nearestHospital.displayDistance()))
                                .replace("{{HOSPITAL_COORDS}}", escapeHtml(nearestHospital.coordinates()))
                                .replace("{{HOSPITAL_LINKS}}", hospitalLinks)
                                .replace("{{MARKET_NAME}}", escapeHtml(nearestMarket.name()))
                                .replace("{{MARKET_DISTANCE}}", escapeHtml(nearestMarket.displayDistance()))
                                .replace("{{MARKET_COORDS}}", escapeHtml(nearestMarket.coordinates()))
                                .replace("{{MARKET_LINKS}}", marketLinks);

        try {
                        MimeMessage mail = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(mail, "UTF-8");
                        helper.setTo(user.getEmail());
                        helper.setSubject("TunisiaHub Booking Confirmation - " + accommodation.getTitle());
                        helper.setText(htmlBody, true);
                        mailSender.send(mail);
        } catch (Exception ex) {
            // Do not block reservation workflow if email fails.
            log.warn("Booking confirmation email failed for {}: {}", user.getEmail(), ex.getMessage());
        }
    }

    public void sendAccommodationStartReminder(
            User user,
            Accommodation accommodation,
            Reservation reservation,
            List<WeatherForecastService.ForecastDay> forecastDays
    ) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank() || accommodation == null || reservation == null) {
            return;
        }

        NearbyPlaceService.PlaceInfo nearestHospital = nearbyPlaceService
                .findNearestHospital(accommodation.getLatitude(), accommodation.getLongitude());
        NearbyPlaceService.PlaceInfo nearestMarket = nearbyPlaceService
                .findNearestMarket(accommodation.getLatitude(), accommodation.getLongitude());

        String fullName = ((user.getPrenom() != null ? user.getPrenom() : "") + " "
                + (user.getNom() != null ? user.getNom() : "")).trim();
        if (fullName.isEmpty()) {
            fullName = "Traveler";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String reservationDates = sdf.format(reservation.getStartDate()) + " to " + sdf.format(reservation.getEndDate());
        String totalPrice = (reservation.getTotalPrice() != null ? reservation.getTotalPrice() : 0) + " TND";

        String hospitalLinks = buildMapLinks(nearestHospital);
        String marketLinks = buildMapLinks(nearestMarket);
        String weatherRows = buildWeatherRows(forecastDays);

        String htmlBodyTemplate = """
                <html>
                    <body style=\"margin:0;padding:0;background:#f4f7fb;font-family:Segoe UI,Arial,sans-serif;color:#1a2233;\">
                        <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">
                            <tr>
                                <td align=\"center\">
                                    <table width=\"700\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:700px;background:#ffffff;border-radius:14px;overflow:hidden;border:1px solid #e7ebf3;\">
                                        <tr>
                                            <td style=\"background:#b91c1c;color:#ffffff;padding:20px 24px;\">
                                                <h2 style=\"margin:0;font-size:24px;\">Your trip starts in 24 hours</h2>
                                                <p style=\"margin:8px 0 0 0;font-size:14px;opacity:.92;\">Here is your stay reminder with weather and nearby essentials.</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style=\"padding:22px 24px;\">
                                                <p style=\"margin:0 0 14px 0;font-size:15px;\">Hello <strong>{{FULL_NAME}}</strong>,</p>

                                                <table width=\"100%\" cellspacing=\"0\" cellpadding=\"8\" style=\"border:1px solid #ecf0f7;border-radius:10px;background:#fbfcff;\">
                                                    <tr><td><strong>Accommodation:</strong> {{ACCOMMODATION_TITLE}}</td></tr>
                                                    <tr><td><strong>Address:</strong> {{ACCOMMODATION_ADDRESS}}</td></tr>
                                                    <tr><td><strong>Type:</strong> {{ACCOMMODATION_TYPE}}</td></tr>
                                                    <tr><td><strong>Dates:</strong> {{RESERVATION_DATES}}</td></tr>
                                                    <tr><td><strong>Total price:</strong> {{TOTAL_PRICE}}</td></tr>
                                                </table>

                                                <h3 style=\"margin:22px 0 10px 0;font-size:18px;color:#b91c1c;\">Weather forecast (next days)</h3>
                                                <table width=\"100%\" cellspacing=\"0\" cellpadding=\"8\" style=\"border:1px solid #ecf0f7;border-radius:10px;background:#fff;\">
                                                    <tr style=\"background:#fff1f2;\">
                                                        <th align=\"left\" style=\"font-size:13px;\">Date</th>
                                                        <th align=\"left\" style=\"font-size:13px;\">Conditions</th>
                                                        <th align=\"left\" style=\"font-size:13px;\">Temperature</th>
                                                    </tr>
                                                    {{WEATHER_ROWS}}
                                                </table>

                                                <h3 style=\"margin:22px 0 10px 0;font-size:18px;color:#0f4c81;\">Nearby essential places</h3>

                                                <table width=\"100%\" cellspacing=\"0\" cellpadding=\"10\" style=\"border-collapse:separate;border-spacing:0 8px;\">
                                                    <tr style=\"background:#f7faff;\">
                                                        <td style=\"border:1px solid #e7eef9;border-radius:8px;\">
                                                            <div style=\"font-size:14px;\"><strong>Nearest hospital:</strong> {{HOSPITAL_NAME}} ({{HOSPITAL_DISTANCE}})</div>
                                                            <div style=\"font-size:13px;color:#42516c;margin-top:6px;\">Coordinates: {{HOSPITAL_COORDS}}</div>
                                                            <div style=\"margin-top:8px;font-size:13px;\">{{HOSPITAL_LINKS}}</div>
                                                        </td>
                                                    </tr>
                                                    <tr style=\"background:#f7faff;\">
                                                        <td style=\"border:1px solid #e7eef9;border-radius:8px;\">
                                                            <div style=\"font-size:14px;\"><strong>Nearest market:</strong> {{MARKET_NAME}} ({{MARKET_DISTANCE}})</div>
                                                            <div style=\"font-size:13px;color:#42516c;margin-top:6px;\">Coordinates: {{MARKET_COORDS}}</div>
                                                            <div style=\"margin-top:8px;font-size:13px;\">{{MARKET_LINKS}}</div>
                                                        </td>
                                                    </tr>
                                                </table>

                                                <p style=\"margin:16px 0 0 0;font-size:14px;color:#495770;\">Safe travels and thank you for choosing TunisiaHub.</p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
                """;

        String htmlBody = htmlBodyTemplate
                .replace("{{FULL_NAME}}", escapeHtml(fullName))
                .replace("{{ACCOMMODATION_TITLE}}", escapeHtml(accommodation.getTitle()))
                .replace("{{ACCOMMODATION_ADDRESS}}", escapeHtml(accommodation.getAdresse()))
                .replace("{{ACCOMMODATION_TYPE}}", escapeHtml(accommodation.getType()))
                .replace("{{RESERVATION_DATES}}", escapeHtml(reservationDates))
                .replace("{{TOTAL_PRICE}}", escapeHtml(totalPrice))
                .replace("{{WEATHER_ROWS}}", weatherRows)
                .replace("{{HOSPITAL_NAME}}", escapeHtml(nearestHospital.name()))
                .replace("{{HOSPITAL_DISTANCE}}", escapeHtml(nearestHospital.displayDistance()))
                .replace("{{HOSPITAL_COORDS}}", escapeHtml(nearestHospital.coordinates()))
                .replace("{{HOSPITAL_LINKS}}", hospitalLinks)
                .replace("{{MARKET_NAME}}", escapeHtml(nearestMarket.name()))
                .replace("{{MARKET_DISTANCE}}", escapeHtml(nearestMarket.displayDistance()))
                .replace("{{MARKET_COORDS}}", escapeHtml(nearestMarket.coordinates()))
                .replace("{{MARKET_LINKS}}", marketLinks);

        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("TunisiaHub Reminder: Your stay starts tomorrow - " + accommodation.getTitle());
            helper.setText(htmlBody, true);
            mailSender.send(mail);
        } catch (Exception ex) {
            throw new IllegalStateException("Reminder email failed: " + ex.getMessage(), ex);
        }
    }

        private String buildMapLinks(NearbyPlaceService.PlaceInfo placeInfo) {
                if (!placeInfo.hasCoordinates()) {
                        return "<span style=\"color:#7a869f;\">No map links available</span>";
                }

                return "<a href=\"" + placeInfo.openStreetMapUrl() + "\" target=\"_blank\" style=\"color:#0f4c81;text-decoration:none;font-weight:600;\">OpenStreetMap</a>"
                                + " &nbsp;|&nbsp; "
                                + "<a href=\"" + placeInfo.googleMapsUrl() + "\" target=\"_blank\" style=\"color:#0f4c81;text-decoration:none;font-weight:600;\">Google Maps</a>";
        }

        private String escapeHtml(String text) {
                if (text == null) {
                        return "";
                }
                return text
                                .replace("&", "&amp;")
                                .replace("<", "&lt;")
                                .replace(">", "&gt;")
                                .replace("\"", "&quot;")
                                .replace("'", "&#39;");
        }

            private String buildWeatherRows(List<WeatherForecastService.ForecastDay> forecastDays) {
                if (forecastDays == null || forecastDays.isEmpty()) {
                    return "<tr><td colspan=\"3\" style=\"font-size:13px;color:#64748b;\">Forecast not available at the moment.</td></tr>";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM");
                StringBuilder rows = new StringBuilder();
                for (WeatherForecastService.ForecastDay day : forecastDays) {
                    rows.append("<tr>")
                            .append("<td style=\"font-size:13px;\">")
                            .append(escapeHtml(day.date().format(formatter)))
                            .append("</td>")
                            .append("<td style=\"font-size:13px;\">")
                            .append(escapeHtml(day.weatherLabel()))
                            .append("</td>")
                            .append("<td style=\"font-size:13px;\">")
                            .append(escapeHtml(String.format("%.1f°C to %.1f°C", day.minTemp(), day.maxTemp())))
                            .append("</td>")
                            .append("</tr>");
                }
                return rows.toString();
            }
}
