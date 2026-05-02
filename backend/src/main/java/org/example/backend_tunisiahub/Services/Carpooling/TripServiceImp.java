package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Carpooling.HolidayCalendar;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class TripServiceImp implements ITripService {

    private TripRepository tripRepository;
    private ReservationRepository reservationRepository;
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TripServiceImp.class);
    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_CANCELED = "canceled";
    private static final String STATUS_COMPLETED = "completed";
    private static final String BOOKING_MODE_INSTANT = "instant";
    private static final String BOOKING_MODE_MANUAL = "manual";
    private static final int MAX_SEATS_TOTAL = 8;
    private static final BigDecimal MAX_PRICE = new BigDecimal("500");
    private static final BigDecimal HOLIDAY_PRICE_MULTIPLIER = new BigDecimal("2");
    private static final int DURATION_MIN_TOLERANCE = 10;
    private static final double DURATION_TOLERANCE_RATE = 0.15d;
    private static final int HOLIDAY_PRICE_WINDOW_DAYS = 2;

    private final IHolidayCalendarService holidayCalendarService;

    @Override
    public List<Trip> retrieveAllTrips(String departurePoint,
                                       String destination,
                                       LocalDate dateFrom,
                                       LocalDate dateTo,
                                       Integer seatsRequired,
                                       String status,
                                       String bookingMode,
                                       BigDecimal minPrice,
                                       BigDecimal maxPrice,
                                       Integer durationMax) {
        LocalDate effectiveDateFrom = dateFrom;
        LocalDate effectiveDateTo = dateTo;
        if (effectiveDateFrom != null && effectiveDateTo != null && effectiveDateFrom.isAfter(effectiveDateTo)) {
            LocalDate temporaryDate = effectiveDateFrom;
            effectiveDateFrom = effectiveDateTo;
            effectiveDateTo = temporaryDate;
        }

        LocalDateTime dateFromValue = effectiveDateFrom != null ? effectiveDateFrom.atStartOfDay() : null;
        LocalDateTime dateToValue = effectiveDateTo != null ? effectiveDateTo.plusDays(1).atStartOfDay() : null;
        Integer normalizedSeatsRequired = seatsRequired != null && seatsRequired > 0 ? seatsRequired : null;
        String normalizedStatus = normalizeStatusFilter(status);
        BigDecimal normalizedMinPrice = minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0 ? minPrice : null;
        BigDecimal normalizedMaxPrice = maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0 ? maxPrice : null;
        Integer normalizedDurationMax = durationMax != null && durationMax > 0 ? durationMax : null;
        String effectiveStatus = normalizedStatus != null ? normalizedStatus : STATUS_SCHEDULED;
        String normalizedBookingMode = normalizeBookingModeFilter(bookingMode);

        return tripRepository.searchTripsAdvanced(
                effectiveStatus,
                normalizeSearchText(departurePoint),
                normalizeSearchText(destination),
                dateFromValue,
                dateToValue,
                normalizedSeatsRequired,
                normalizedBookingMode,
                normalizedMinPrice,
                normalizedMaxPrice,
                normalizedDurationMax
        );
    }

    @Override
    public Trip retrieveTrip(Long id) {
        logger.debug("Retrieve public trip id={}", id);
        return tripRepository.findById(id).orElse(null);
    }

    @Override
    public Trip retrieveMyTrip(Long id, Long driverId) {
        logger.debug("Retrieve driver trip id={} driverId={}", id, driverId);
        return tripRepository.findByIdAndDriverId(id, driverId);
    }

    @Override
    public List<Trip> retrieveMyTrips(Long driverId) {
        logger.debug("Retrieve trips for driverId={}", driverId);
        return tripRepository.findByDriverIdOrderByDepartureDateTimeDesc(driverId);
    }

    @Override
    public Integer retrieveTripSeatsAvailable(Long tripId) {
        return tripRepository.findSeatsAvailableByTripId(tripId);
    }

    @Override
    public TripPriceSuggestion retrievePriceSuggestion(String departure,
                                                       String destination,
                                                       LocalDate departureDate,
                                                       Integer durationMinutes) {
        if (departure == null || departure.isBlank()
                || destination == null || destination.isBlank()
                || departureDate == null
                || durationMinutes == null || durationMinutes < 1) {
            return null;
        }

        String normalizedDeparture = normalizeMainLocation(departure);
        String normalizedDestination = normalizeMainLocation(destination);
        if (normalizedDeparture.isBlank() || normalizedDestination.isBlank()) {
            logger.info("Price suggestion skipped because normalized route is blank departure={} destination={}",
                    departure, destination);
            return null;
        }

        List<Trip> historicalTrips = tripRepository
                .findByDepartureDateTimeBeforeOrderByDepartureDateTimeAsc(departureDate.atStartOfDay())
                .stream()
                .filter(this::isTripPriceHistoryUsable)
                .toList();
        if (historicalTrips.isEmpty()) {
            logger.info("Price suggestion skipped because no historical trips were found route={} -> {} date={} duration={}",
                    normalizedDeparture, normalizedDestination, departureDate, durationMinutes);
            return null;
        }

        List<Trip> sameRouteTrips = historicalTrips.stream()
                .filter(trip -> normalizeMainLocation(trip.getDeparture()).equals(normalizedDeparture)
                        && normalizeMainLocation(trip.getDestination()).equals(normalizedDestination))
                .toList();
        List<Trip> candidatePool = !sameRouteTrips.isEmpty() ? sameRouteTrips : historicalTrips;
        List<Trip> similarTrips = findSimilarDurationTrips(candidatePool, durationMinutes);
        if (similarTrips.isEmpty()) {
            logger.info("Price suggestion skipped because no similar trips matched route={} -> {} date={} duration={} candidatePoolSize={}",
                    normalizedDeparture, normalizedDestination, departureDate, durationMinutes, candidatePool.size());
            return null;
        }

        logger.info(
                "Price suggestion request route={} -> {} date={} duration={} historicalTrips={} sameRouteTrips={} candidatePoolType={} candidatePoolSize={} similarTrips={}",
                normalizedDeparture,
                normalizedDestination,
                departureDate,
                durationMinutes,
                historicalTrips.size(),
                sameRouteTrips.size(),
                !sameRouteTrips.isEmpty() ? "same_route" : "all_history",
                candidatePool.size(),
                similarTrips.size()
        );
        for (Trip trip : similarTrips) {
            logger.info(
                    "Price comparison tripId={} route={} -> {} tripDateTime={} duration={} price={} durationDifference={}",
                    trip.getId(),
                    normalizeMainLocation(trip.getDeparture()),
                    normalizeMainLocation(trip.getDestination()),
                    trip.getDepartureDateTime(),
                    trip.getDurationMinutes(),
                    trip.getPrice(),
                    Math.abs(trip.getDurationMinutes() - durationMinutes)
            );
        }

        BigDecimal basePrice = calculateAveragePrice(similarTrips);
        HolidayCalendar holiday = findCriticalHolidayForDate(departureDate);
        BigDecimal suggestedPrice = holiday != null
                ? basePrice.multiply(HOLIDAY_PRICE_MULTIPLIER)
                : basePrice;

        BigDecimal minHistoricalPrice = similarTrips.stream()
                .map(Trip::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(basePrice);
        BigDecimal maxHistoricalPrice = similarTrips.stream()
                .map(Trip::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(basePrice);

        logger.info(
                "Price suggestion result route={} -> {} date={} duration={} averagePrice={} minHistoricalPrice={} maxHistoricalPrice={} holiday={} holidayMultiplier={} suggestedPriceBeforeRound={} suggestedPriceRounded={}",
                normalizedDeparture,
                normalizedDestination,
                departureDate,
                durationMinutes,
                basePrice,
                minHistoricalPrice,
                maxHistoricalPrice,
                holiday != null ? holiday.getHolidayName() : "none",
                holiday != null ? HOLIDAY_PRICE_MULTIPLIER : BigDecimal.ONE,
                suggestedPrice,
                roundSuggestedPrice(suggestedPrice)
        );

        return new TripPriceSuggestion(
                suggestedPrice,
                basePrice,
                minHistoricalPrice,
                maxHistoricalPrice,
                similarTrips.size(),
                holiday != null,
                holiday != null ? holiday.getHolidayName() : null,
                buildPriceSuggestionMessage(similarTrips.size(), holiday)
        );
    }

    @Override
    public Trip addTrip(Trip request, Long driverId) {
        if (!validateTrip(request, 0)) {
            logger.warn("Trip validation failed on create for driverId={}", driverId);
            return null;
        }

        User driver = userRepository.findById(driverId).orElse(null);
        if (driver == null) {
            logger.warn("Trip creation failed because driver not found driverId={}", driverId);
            return null;
        }

        Trip trip = new Trip();
        trip.setDeparture(request.getDeparture().trim());
        trip.setDeparturePoint(request.getDeparture().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        if (request.getDepartureDateTime() != null) {
            trip.setDepartureTime(request.getDepartureDateTime().toLocalTime().toString());
        }
        trip.setDurationMinutes(request.getDurationMinutes());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        // initialize seatsAvailable to seatsTotal on creation
        trip.setSeatsAvailable(request.getSeatsTotal());
        trip.setStatus(STATUS_SCHEDULED);
        trip.setBookingMode(normalizeBookingMode(request.getBookingMode()));
        trip.setDriver(driver);
        trip.setCreatedBy(driver.getEmail());
        logger.debug("Saving new trip for driverId={} departure={} destination={}",
                driverId, trip.getDeparture(), trip.getDestination());
        return tripRepository.save(trip);
    }

    @Override
    public Trip modifyTrip(Long tripId, Trip request, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip update failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip update rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        int reservedSeats = getReservedSeats(tripId);
        if (!validateTrip(request, reservedSeats)) {
            logger.warn("Trip validation failed on update id={} userId={} reservedSeats={}",
                    tripId, currentUserId, reservedSeats);
            return null;
        }

        trip.setDeparture(request.getDeparture().trim());
        trip.setDestination(request.getDestination().trim());
        trip.setDepartureDateTime(request.getDepartureDateTime());
        trip.setDurationMinutes(request.getDurationMinutes());
        trip.setPrice(request.getPrice());
        trip.setSeatsTotal(request.getSeatsTotal());
        // adjust seatsAvailable when seatsTotal changes, keeping reserved seats accounted for
        int available = trip.getSeatsTotal() - reservedSeats;
        trip.setSeatsAvailable(Math.max(0, available));
        if (request.getBookingMode() != null && !request.getBookingMode().isBlank()) {
            trip.setBookingMode(normalizeBookingMode(request.getBookingMode()));
        }
        logger.debug("Saving updated trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip cancelTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip cancel failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip cancel rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        trip.setStatus(STATUS_CANCELED);
        cancelTripReservations(tripId);
        logger.debug("Saving canceled trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip makeTripAvailable(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip restore failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip restore rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        trip.setStatus(STATUS_SCHEDULED);
        confirmTripReservations(tripId);
        logger.debug("Saving restored trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    @Override
    public Trip completeTrip(Long tripId, Long currentUserId) {
        Trip trip = tripRepository.findById(tripId).orElse(null);
        if (trip == null) {
            logger.warn("Trip complete failed because trip not found id={}", tripId);
            return null;
        }
        if (!isOwner(trip, currentUserId)) {
            logger.warn("Trip complete rejected because userId={} is not owner of tripId={}", currentUserId, tripId);
            return null;
        }
        if (STATUS_CANCELED.equalsIgnoreCase(trip.getStatus())) {
            logger.warn("Trip complete rejected because trip is canceled id={}", tripId);
            return null;
        }

        trip.setStatus(STATUS_COMPLETED);
        logger.debug("Saving completed trip id={} userId={}", tripId, currentUserId);
        return tripRepository.save(trip);
    }

    private boolean isOwner(Trip trip, Long currentUserId) {
        return trip.getDriver() != null && currentUserId.equals(trip.getDriver().getId());
    }

    private boolean isTripPriceHistoryUsable(Trip trip) {
        if (trip == null) {
            return false;
        }
        if (trip.getDepartureDateTime() == null) {
            return false;
        }
        if (trip.getDurationMinutes() == null || trip.getDurationMinutes() < 1) {
            return false;
        }
        if (trip.getPrice() == null || trip.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return trip.getStatus() == null || !STATUS_CANCELED.equalsIgnoreCase(trip.getStatus());
    }

    private List<Trip> findSimilarDurationTrips(List<Trip> trips, Integer requestedDurationMinutes) {
        int tolerance = Math.max(DURATION_MIN_TOLERANCE, (int) Math.round(requestedDurationMinutes * DURATION_TOLERANCE_RATE));
        List<Trip> matchedTrips = trips.stream()
                .filter(trip -> Math.abs(trip.getDurationMinutes() - requestedDurationMinutes) <= tolerance)
                .sorted(Comparator.comparingInt(trip -> Math.abs(trip.getDurationMinutes() - requestedDurationMinutes)))
                .limit(8)
                .toList();

        if (matchedTrips.size() >= 3) {
            return matchedTrips;
        }

        return trips.stream()
                .sorted(Comparator.comparingInt(trip -> Math.abs(trip.getDurationMinutes() - requestedDurationMinutes)))
                .filter(trip -> Math.abs(trip.getDurationMinutes() - requestedDurationMinutes) <= DURATION_MIN_TOLERANCE * 2)
                .limit(5)
                .toList();
    }

    private BigDecimal calculateAveragePrice(List<Trip> trips) {
        if (trips == null || trips.isEmpty()) {
            return BigDecimal.ONE;
        }

        BigDecimal totalPrice = trips.stream()
                .map(Trip::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPrice.divide(
                BigDecimal.valueOf(trips.size()),
                10,
                RoundingMode.HALF_UP
        );
    }

    private HolidayCalendar findCriticalHolidayForDate(LocalDate departureDate) {
        return holidayCalendarService.retrieveHolidaysByYear(departureDate.getYear()).stream()
                .filter(holiday -> Boolean.TRUE.equals(holiday.getCriticalForCarpooling()))
                .filter(holiday -> {
                    long daysDifference = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                            holiday.getHolidayDate(),
                            departureDate
                    ));
                    return daysDifference <= HOLIDAY_PRICE_WINDOW_DAYS;
                })
                .min(Comparator.comparingLong(holiday -> Math.abs(java.time.temporal.ChronoUnit.DAYS.between(
                        holiday.getHolidayDate(),
                        departureDate
                ))))
                .orElse(null);
    }

    private BigDecimal roundSuggestedPrice(BigDecimal value) {
        BigDecimal rounded = value.setScale(0, RoundingMode.HALF_UP);
        if (rounded.compareTo(BigDecimal.ONE) < 0) {
            return BigDecimal.ONE;
        }
        if (rounded.compareTo(MAX_PRICE) > 0) {
            return MAX_PRICE;
        }
        return rounded;
    }

    private String buildPriceSuggestionMessage(int similarTripsCount, HolidayCalendar holiday) {
        if (holiday != null) {
            return "Suggested from " + similarTripsCount
                    + " previous trips with similar duration. "
                    + holiday.getHolidayName()
                    + " is within 2 days of this trip date, so a slightly higher price is suggested.";
        }

        return "Suggested from " + similarTripsCount
                + " previous trips with similar duration.";
    }

    private String normalizeMainLocation(String value) {
        if (value == null) {
            return "";
        }

        String cleaned = value.trim();
        int commaIndex = cleaned.indexOf(',');
        if (commaIndex >= 0) {
            cleaned = cleaned.substring(0, commaIndex);
        }

        return cleaned.trim().toLowerCase(Locale.ROOT);
    }

    private boolean validateTrip(Trip request, int reservedSeats) {
        if (request == null) {
            return false;
        }
        if (request.getDeparture() == null || request.getDeparture().isBlank()) {
            return false;
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            return false;
        }
        if (normalizeLocation(request.getDeparture()).equals(normalizeLocation(request.getDestination()))) {
            return false;
        }
        if (request.getDepartureDateTime() == null || !request.getDepartureDateTime().isAfter(LocalDateTime.now())) {
            return false;
        }
        if (request.getDurationMinutes() == null || request.getDurationMinutes() < 1) {
            return false;
        }
        BigDecimal price = request.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            return false;
        }
        if (request.getSeatsTotal() < 1) {
            return false;
        }
        if (request.getSeatsTotal() > MAX_SEATS_TOTAL) {
            return false;
        }
        if (request.getSeatsTotal() < reservedSeats) {
            return false;
        }
        return true;
    }

    private int getReservedSeats(Long tripId) {
        return reservationRepository.findByTripId(tripId).stream()
                .filter(this::isActiveTripReservation)
                .mapToInt(this::getReservedPeopleCount)
                .sum();
    }

    private boolean isActiveTripReservation(Reservation reservation) {
        if (reservation == null) {
            return false;
        }

        return reservation.getStatus() != ReservationStatus.CANCELLED;
    }

    private void cancelTripReservations(Long tripId) {
        List<Reservation> reservations = reservationRepository.findByTripId(tripId);
        for (Reservation reservation : reservations) {
            if (isActiveTripReservation(reservation)) {
                reservation.setStatus(ReservationStatus.CANCELLED);
            }
        }
        reservationRepository.saveAll(reservations);
    }

    private void confirmTripReservations(Long tripId) {
        List<Reservation> reservations = reservationRepository.findByTripId(tripId);
        for (Reservation reservation : reservations) {
            if (!isActiveTripReservation(reservation)) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
            }
        }
        reservationRepository.saveAll(reservations);
    }

    private int getReservedPeopleCount(Reservation reservation) {
        Integer numberOfPeople = reservation.getNumberOfPeople();
        if (numberOfPeople == null || numberOfPeople < 1) {
            return 1;
        }
        return numberOfPeople;
    }

    private String normalizeLocation(String value) {
        return value.trim().toLowerCase();
    }

    private String normalizeSearchText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeBookingMode(String value) {
        if (value == null || value.isBlank()) {
            return BOOKING_MODE_MANUAL;
        }

        String normalized = value.trim().toLowerCase();
        if (BOOKING_MODE_INSTANT.equals(normalized)) {
            return BOOKING_MODE_INSTANT;
        }

        return BOOKING_MODE_MANUAL;
    }

    private String normalizeBookingModeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();
        if (BOOKING_MODE_INSTANT.equals(normalized) || BOOKING_MODE_MANUAL.equals(normalized)) {
            return normalized;
        }

        return null;
    }

    private String normalizeStatusFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase();
        if (STATUS_SCHEDULED.equals(normalized) || STATUS_CANCELED.equals(normalized) || STATUS_COMPLETED.equals(normalized)) {
            return normalized;
        }

        return null;
    }
}
