package org.example.backend_tunisiahub.Services.Accommodation.Notification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccommodationReminderScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservationEmailService reservationEmailService;
    private final WeatherForecastService weatherForecastService;

    @Value("${app.reminder.timezone:Africa/Tunis}")
    private String reminderTimezone;

    @Scheduled(
            cron = "${app.reminder.accommodation.cron:0 */15 * * * *}",
            zone = "${app.reminder.timezone:Africa/Tunis}"
    )
    @Transactional
    public void send24hAccommodationReminders() {
        ZoneId zoneId = resolveZoneId(reminderTimezone);
        LocalDate targetDateLocal = LocalDate.now(zoneId).plusDays(1);
        Date targetDate = java.sql.Date.valueOf(targetDateLocal);

        List<Reservation> pendingReservations = reservationRepository.findPendingAccommodationReminderReservations(
                ReservationType.accommodationReservation,
                targetDate
        );

        if (pendingReservations.isEmpty()) {
            return;
        }

        log.info("Found {} accommodation reservations pending 24h reminder for {}", pendingReservations.size(), targetDateLocal);

        for (Reservation reservation : pendingReservations) {
            try {
                List<WeatherForecastService.ForecastDay> forecast = weatherForecastService.getForecastForStay(
                        reservation.getAccommodation().getLatitude(),
                        reservation.getAccommodation().getLongitude(),
                        targetDateLocal,
                        3
                );

                reservationEmailService.sendAccommodationStartReminder(
                        reservation.getUser(),
                        reservation.getAccommodation(),
                        reservation,
                        forecast
                );

                reservation.setReminderSentAt(new Date());
                reservation.setReminderStatus("SENT");
                reservation.setReminderError(null);
            } catch (Exception ex) {
                reservation.setReminderStatus("FAILED");
                reservation.setReminderError(truncate(ex.getMessage(), 500));
                log.warn("Failed to send reminder for reservation {}: {}", reservation.getId(), ex.getMessage());
            }
        }

        reservationRepository.saveAll(pendingReservations);
    }

    private ZoneId resolveZoneId(String zone) {
        try {
            return ZoneId.of(zone);
        } catch (Exception ignored) {
            return ZoneId.of("Africa/Tunis");
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
