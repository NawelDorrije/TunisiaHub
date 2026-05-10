package org.example.backend_tunisiahub.Services.Camping.Pricing;

import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.PricingContext;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Assembles every pricing signal into a {@link //PricingContext} for a given
 * Spot and check-in date.
 */
@Service
public class PricingSignalService {

    @Autowired private WeatherService       weatherService;
    @Autowired private SpotRepository       spotRepository;
    @Autowired private ReservationRepository reservationRepository;

    public PricingContext buildContext(Spot spot, LocalDate checkInDate) {

        Camping camping = spot.getCamping();

        // ── 1. Weather score (Open-Meteo free API) ────────────────────────────
        double weatherScore = weatherService.fetchWeatherScore(
                camping.getLatitude(), camping.getLongitude());

        // ── 2. Occupancy rate ─────────────────────────────────────────────────
        long totalSpots  = spotRepository.countByCampingId(camping.getId());
        long bookedSpots = reservationRepository
                .countActiveReservationsForCampingOnDate(camping.getId(), checkInDate);
        double occupancy = totalSpots > 0 ? (double) bookedSpots / totalSpots : 0.0;

        // ── 3. Demand index (this week vs. same week last year) ───────────────
        LocalDate now           = LocalDate.now();
        LocalDate weekAgo       = now.minusDays(7);
        LocalDate lastYearNow   = now.minusYears(1);
        LocalDate lastYearWeek  = weekAgo.minusYears(1);

        long recentBookings   = reservationRepository
                .countBookingsMadeBetween(camping.getId(), weekAgo, now);
        long historicBookings = reservationRepository
                .countBookingsMadeBetween(camping.getId(), lastYearWeek, lastYearNow);

        double demandIndex = historicBookings > 0
                ? (double) recentBookings / historicBookings
                : (recentBookings > 0 ? 1.5 : 1.0);

        // ── 4. Local event / public holiday detection ─────────────────────────
        boolean localEvent = isTunisianHolidayOrPeakSeason(checkInDate);

        // ── 5. Time signals ───────────────────────────────────────────────────
        DayOfWeek dayOfWeek      = checkInDate.getDayOfWeek();
        int daysUntilCheckIn     = Math.max(0,
                (int) (checkInDate.toEpochDay() - now.toEpochDay()));

        return new PricingContext(
                spot.getId(),
                camping.getId(),
                spot.getBasePrice(),
                weatherScore,
                occupancy,
                demandIndex,
                localEvent,
                dayOfWeek,
                daysUntilCheckIn
        );
    }

    /**
     * Returns true for Tunisian public holidays and known peak camping periods.
     * Extend this list as needed.
     */
    private boolean isTunisianHolidayOrPeakSeason(LocalDate date) {
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();

        // Fixed national holidays
        if ((m == 1  && d == 1)  ||   // New Year
                (m == 3  && d == 20) ||   // Independence Day
                (m == 4  && d == 9)  ||   // Martyrs' Day
                (m == 5  && d == 1)  ||   // Labour Day
                (m == 7  && d == 25) ||   // Republic Day
                (m == 8  && d == 13) ||   // Women's Day
                (m == 10 && d == 15))     // Evacuation Day
            return true;

        // Peak summer camping season
        if (m == 7 || m == 8) return true;

        // Spring school break (approximate)
        if (m == 4 && d >= 1 && d <= 15) return true;

        return false;
    }
}