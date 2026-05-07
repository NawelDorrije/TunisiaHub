package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Carpooling.HolidayCalendar;
import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Repositories.Carpooling.TripRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class TripDemandServiceImp implements ITripDemandService {

    private static final Logger logger = LoggerFactory.getLogger(TripDemandServiceImp.class);
    private static final DateTimeFormatter ALERT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String STATUS_CANCELED = "canceled";
    private static final String STATUS_CANCELLED = "cancelled";
    private static final String STATUS_PENDING = "pending";
    private static final String MODEL_NAME = "Linear Regression";
    private static final double REGULARIZATION = 0.15d;
    private static final int ROUTE_HISTORY_DAYS_SHORT = 7;
    private static final int ROUTE_HISTORY_DAYS_LONG = 30;
    private static final int HOLIDAY_LOOKAHEAD_DAYS = 7;
    private static final int HOLIDAY_ALERT_WINDOW_DAYS = 2;
    private static final int HOLIDAY_MAX_DAYS = 30;
    private static final int HOLIDAY_FEATURES_COUNT = 6;
    private static final double HOLIDAY_ALERT_MIN_SEATS_DIFF = 0.35d;
    private static final int BETTER_PERIOD_SEARCH_DAYS = 30;
    private static final double BETTER_PERIOD_MIN_OCCUPANCY_IMPROVEMENT = 0.15d;

    private final TripRepository tripRepository;
    private final ReservationRepository reservationRepository;
    private final IHolidayCalendarService holidayCalendarService;

    @Override
    public TripDemandAlert retrieveDemandAlert(String departure, String destination, LocalDate dateFrom, LocalDate dateTo) {
        String normalizedDeparture = normalizeLocation(departure);
        String normalizedDestination = normalizeLocation(destination);
        if (normalizedDeparture.isBlank() || normalizedDestination.isBlank()) {
            return null;
        }

        LocalDate effectiveDate = resolveEffectiveDate(dateFrom, dateTo);
        DateRange dateRange = resolveDateRange(dateFrom, dateTo, effectiveDate);
        LocalDateTime predictionMoment = effectiveDate.atStartOfDay();
        String periodLabel = buildPeriodLabel(dateFrom, dateTo, effectiveDate);
        Map<Integer, List<HolidayCalendar>> holidayCache = new HashMap<>();
        HolidayEventInfo holidayEventInfo = findRelevantHoliday(dateRange.startDate(), dateRange.endDate(), holidayCache);
        List<Trip> historicalTrips = tripRepository
                .findByDepartureDateTimeBeforeOrderByDepartureDateTimeAsc(predictionMoment)
                .stream()
                .filter(this::isHistoricalTripUsable)
                .sorted(Comparator.comparing(Trip::getDepartureDateTime))
                .toList();

        if (historicalTrips.isEmpty()) {
            return buildFallbackAlert(normalizedDeparture, normalizedDestination, effectiveDate, periodLabel, holidayEventInfo);
        }

        Map<Long, Integer> confirmedSeatsByTripId = buildConfirmedSeatsByTripId();
        List<TrainingSample> trainingSamples = buildTrainingSamples(historicalTrips, confirmedSeatsByTripId, holidayCache);
        if (trainingSamples.isEmpty()) {
            return buildFallbackAlert(normalizedDeparture, normalizedDestination, effectiveDate, periodLabel, holidayEventInfo);
        }

        double[] coefficients = trainLinearRegression(trainingSamples);
        RouteDemandStats routeDemandStats = buildRouteDemandStats(
                historicalTrips,
                confirmedSeatsByTripId,
                normalizedDeparture,
                normalizedDestination,
                effectiveDate
        );
        if (routeDemandStats.totalTrips() == 0) {
            return buildNoRouteHistoryAlert(
                    normalizedDeparture,
                    normalizedDestination,
                    effectiveDate,
                    trainingSamples.size(),
                    periodLabel,
                    holidayEventInfo
            );
        }

        double referenceSeats = routeDemandStats.averageSeatsTotal() > 0
                ? routeDemandStats.averageSeatsTotal()
                : estimateReferenceSeats(
                historicalTrips,
                normalizedDeparture,
                normalizedDestination,
                effectiveDate
        );
        DemandPredictionResult predictionResult = predictDemandForRange(
                coefficients,
                historicalTrips,
                normalizedDeparture,
                normalizedDestination,
                dateRange.startDate(),
                dateRange.endDate(),
                holidayCache,
                routeDemandStats,
                referenceSeats
        );
        double safePredictedSeats = predictionResult.predictedSeats();
        double safePredictedSeatsWithoutHoliday = predictionResult.predictedSeatsWithoutHoliday();
        double occupancyRate = predictionResult.occupancyRate();
        String demandLevel = resolveDemandLevel(occupancyRate);
        boolean holidayBoostActive = holidayEventInfo != null
                && (
                holidayEventInfo.insideRange()
                        || (safePredictedSeats - safePredictedSeatsWithoutHoliday) >= HOLIDAY_ALERT_MIN_SEATS_DIFF
        );
        SuggestedPeriodInfo suggestedPeriodInfo = findBetterPeriodSuggestion(
                demandLevel,
                holidayEventInfo,
                coefficients,
                historicalTrips,
                normalizedDeparture,
                normalizedDestination,
                dateRange.startDate(),
                holidayCache,
                routeDemandStats,
                referenceSeats,
                occupancyRate
        );

        logger.info(
                "Demand alert route={} -> {} range={} to {} level={} predictedSeats={} predictedSeatsWithoutHoliday={} holiday={} holidayInsideRange={} holidayBoostActive={}",
                formatLocation(normalizedDeparture),
                formatLocation(normalizedDestination),
                dateRange.startDate(),
                dateRange.endDate(),
                demandLevel,
                roundValue(safePredictedSeats),
                roundValue(safePredictedSeatsWithoutHoliday),
                holidayEventInfo != null ? holidayEventInfo.holidayName() : "none",
                holidayEventInfo != null && holidayEventInfo.insideRange(),
                holidayBoostActive
        );
        if (suggestedPeriodInfo != null) {
            logger.info("Better period suggestion route={} -> {} selectedDate={} suggestedDate={} selectedOccupancy={} suggestedOccupancy={}",
                    formatLocation(normalizedDeparture),
                    formatLocation(normalizedDestination),
                    dateRange.startDate(),
                    suggestedPeriodInfo.date(),
                    roundValue(occupancyRate),
                    roundValue(suggestedPeriodInfo.predictedOccupancyRate()));
        }

        return new TripDemandAlert(
                formatLocation(normalizedDeparture),
                formatLocation(normalizedDestination),
                buildWeekLabel(effectiveDate),
                demandLevel,
                roundValue(safePredictedSeats),
                roundValue(referenceSeats),
                roundValue(occupancyRate),
                trainingSamples.size(),
                MODEL_NAME,
                holidayEventInfo != null,
                buildPassengerAlert(demandLevel, periodLabel, holidayEventInfo, holidayBoostActive),
                buildDriverAlert(demandLevel, periodLabel, holidayEventInfo, holidayBoostActive, suggestedPeriodInfo),
                suggestedPeriodInfo != null ? suggestedPeriodInfo.date().toString() : null,
                suggestedPeriodInfo != null ? suggestedPeriodInfo.date().toString() : null,
                suggestedPeriodInfo != null ? roundValue(suggestedPeriodInfo.predictedOccupancyRate()) : null
        );
    }

    private TripDemandAlert buildNoRouteHistoryAlert(String departure,
                                                     String destination,
                                                     LocalDate date,
                                                     int trainingSamples,
                                                     String periodLabel,
                                                     HolidayEventInfo holidayEventInfo) {
        String routeLabel = formatLocation(departure) + " → " + formatLocation(destination);
        return new TripDemandAlert(
                formatLocation(departure),
                formatLocation(destination),
                buildWeekLabel(date),
                "low",
                0d,
                3d,
                0d,
                trainingSamples,
                MODEL_NAME,
                holidayEventInfo != null,
                holidayEventInfo != null
                        ? buildPassengerAlert("low", periodLabel, holidayEventInfo, true)
                        : "We are still collecting enough demand history for " + routeLabel + " " + periodLabel + ".",
                holidayEventInfo != null
                        ? buildDriverAlert("low", periodLabel, holidayEventInfo, true, null)
                        : "We are still collecting enough demand history for " + routeLabel + " " + periodLabel + ".",
                null,
                null,
                null
        );
    }

    private TripDemandAlert buildFallbackAlert(String departure,
                                               String destination,
                                               LocalDate date,
                                               String periodLabel,
                                               HolidayEventInfo holidayEventInfo) {
        return new TripDemandAlert(
                formatLocation(departure),
                formatLocation(destination),
                buildWeekLabel(date),
                "low",
                0d,
                3d,
                0d,
                0,
                MODEL_NAME,
                holidayEventInfo != null,
                holidayEventInfo != null
                        ? buildPassengerAlert("low", periodLabel, holidayEventInfo, true)
                        : "We are still collecting enough demand history for this route " + periodLabel + ".",
                holidayEventInfo != null
                        ? buildDriverAlert("low", periodLabel, holidayEventInfo, true, null)
                        : "We are still collecting enough route history to estimate demand " + periodLabel + ".",
                null,
                null,
                null
        );
    }

    private DemandPredictionResult predictDemandForRange(double[] coefficients,
                                                         List<Trip> historicalTrips,
                                                         String departure,
                                                         String destination,
                                                         LocalDate startDate,
                                                         LocalDate endDate,
                                                         Map<Integer, List<HolidayCalendar>> holidayCache,
                                                         RouteDemandStats routeDemandStats,
                                                         double referenceSeats) {
        double[] predictionFeatures = buildPredictionFeatures(
                historicalTrips,
                departure,
                destination,
                startDate,
                endDate,
                holidayCache,
                true
        );
        double[] predictionFeaturesWithoutHoliday = buildPredictionFeatures(
                historicalTrips,
                departure,
                destination,
                startDate,
                endDate,
                holidayCache,
                false
        );
        double predictedSeatsBooked = predict(coefficients, predictionFeatures);
        double predictedSeatsWithoutHoliday = predict(coefficients, predictionFeaturesWithoutHoliday);
        double blendedPredictedSeats = blendPredictedSeats(
                predictedSeatsBooked,
                routeDemandStats.averageBookedSeats(),
                routeDemandStats.totalTrips()
        );
        double blendedPredictedSeatsWithoutHoliday = blendPredictedSeats(
                predictedSeatsWithoutHoliday,
                routeDemandStats.averageBookedSeats(),
                routeDemandStats.totalTrips()
        );
        double safePredictedSeats = clamp(blendedPredictedSeats, 0, Math.max(referenceSeats, 1d));
        double safePredictedSeatsWithoutHoliday = clamp(blendedPredictedSeatsWithoutHoliday, 0, Math.max(referenceSeats, 1d));
        double occupancyRate = referenceSeats <= 0 ? 0 : safePredictedSeats / referenceSeats;
        occupancyRate = adjustOccupancyRate(occupancyRate, routeDemandStats);

        return new DemandPredictionResult(safePredictedSeats, safePredictedSeatsWithoutHoliday, occupancyRate);
    }

    private SuggestedPeriodInfo findBetterPeriodSuggestion(String demandLevel,
                                                           HolidayEventInfo holidayEventInfo,
                                                           double[] coefficients,
                                                           List<Trip> historicalTrips,
                                                           String departure,
                                                           String destination,
                                                           LocalDate selectedDate,
                                                           Map<Integer, List<HolidayCalendar>> holidayCache,
                                                           RouteDemandStats routeDemandStats,
                                                           double referenceSeats,
                                                           double selectedOccupancyRate) {
        boolean shouldSuggestBetterPeriod = "low".equalsIgnoreCase(demandLevel)
                || "medium".equalsIgnoreCase(demandLevel);
        if (!shouldSuggestBetterPeriod || holidayEventInfo != null) {
            return null;
        }

        LocalDate bestDate = null;
        double bestOccupancyRate = selectedOccupancyRate;

        logger.info(
                "Better period search started route={} -> {} selectedDate={} selectedDemandLevel={} selectedOccupancy={} searchDays={} minImprovement={}",
                formatLocation(departure),
                formatLocation(destination),
                selectedDate,
                demandLevel,
                roundValue(selectedOccupancyRate),
                BETTER_PERIOD_SEARCH_DAYS,
                BETTER_PERIOD_MIN_OCCUPANCY_IMPROVEMENT
        );

        for (int dayOffset = 1; dayOffset <= BETTER_PERIOD_SEARCH_DAYS; dayOffset++) {
            LocalDate candidateDate = selectedDate.plusDays(dayOffset);
            DemandPredictionResult candidatePrediction = predictDemandForRange(
                    coefficients,
                    historicalTrips,
                    departure,
                    destination,
                    candidateDate,
                    candidateDate,
                    holidayCache,
                    routeDemandStats,
                    referenceSeats
            );
            HolidayEventInfo candidateHoliday = findClosestCriticalHoliday(candidateDate, holidayCache);
            logger.info(
                    "Better period candidate route={} -> {} candidateDate={} dayOffset={} occupancy={} predictedSeats={} predictedSeatsWithoutHoliday={} holiday={} holidayDate={} daysFromHoliday={} newBest={}",
                    formatLocation(departure),
                    formatLocation(destination),
                    candidateDate,
                    dayOffset,
                    roundValue(candidatePrediction.occupancyRate()),
                    roundValue(candidatePrediction.predictedSeats()),
                    roundValue(candidatePrediction.predictedSeatsWithoutHoliday()),
                    candidateHoliday != null ? candidateHoliday.holidayName() : "none",
                    candidateHoliday != null ? candidateHoliday.holidayDate() : null,
                    candidateHoliday != null ? daysFromRange(candidateDate, candidateHoliday.holidayDate(), candidateHoliday.holidayDate()) : null,
                    candidatePrediction.occupancyRate() > bestOccupancyRate
            );
            if (candidatePrediction.occupancyRate() > bestOccupancyRate) {
                bestOccupancyRate = candidatePrediction.occupancyRate();
                bestDate = candidateDate;
            }
        }

        if (bestDate == null || bestOccupancyRate < selectedOccupancyRate + BETTER_PERIOD_MIN_OCCUPANCY_IMPROVEMENT) {
            logger.info(
                    "Better period search rejected route={} -> {} selectedDate={} bestDate={} selectedOccupancy={} bestOccupancy={} requiredMinimum={}",
                    formatLocation(departure),
                    formatLocation(destination),
                    selectedDate,
                    bestDate,
                    roundValue(selectedOccupancyRate),
                    roundValue(bestOccupancyRate),
                    roundValue(selectedOccupancyRate + BETTER_PERIOD_MIN_OCCUPANCY_IMPROVEMENT)
            );
            return null;
        }

        logger.info(
                "Better period search selected route={} -> {} selectedDate={} suggestedDate={} selectedOccupancy={} suggestedOccupancy={} improvement={}",
                formatLocation(departure),
                formatLocation(destination),
                selectedDate,
                bestDate,
                roundValue(selectedOccupancyRate),
                roundValue(bestOccupancyRate),
                roundValue(bestOccupancyRate - selectedOccupancyRate)
        );

        String message = "medium".equalsIgnoreCase(demandLevel)
                ? "People use this route regularly, but " + formatAlertDate(bestDate) + " may help you get passengers faster."
                : "This period looks weak. " + formatAlertDate(bestDate) + " may help you get passengers faster.";
        return new SuggestedPeriodInfo(bestDate, bestOccupancyRate, message);
    }

    private RouteDemandStats buildRouteDemandStats(List<Trip> historicalTrips,
                                                   Map<Long, Integer> confirmedSeatsByTripId,
                                                   String departure,
                                                   String destination,
                                                   LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime longWindow = startDateTime.minusDays(ROUTE_HISTORY_DAYS_LONG);
        int totalTrips = 0;
        int tripsLast30Days = 0;
        double bookedSeatsTotal = 0;
        double seatsTotal = 0;

        for (Trip trip : historicalTrips) {
            if (trip.getDepartureDateTime() == null || !trip.getDepartureDateTime().isBefore(startDateTime)) {
                continue;
            }

            if (!normalizeLocation(trip.getDeparture()).equals(departure)
                    || !normalizeLocation(trip.getDestination()).equals(destination)) {
                continue;
            }

            totalTrips++;
            bookedSeatsTotal += confirmedSeatsByTripId.getOrDefault(trip.getId(), 0);
            seatsTotal += trip.getSeatsTotal();
            if (!trip.getDepartureDateTime().isBefore(longWindow)) {
                tripsLast30Days++;
            }
        }

        double averageBookedSeats = totalTrips > 0 ? bookedSeatsTotal / totalTrips : 0;
        double averageSeatsTotal = totalTrips > 0 ? seatsTotal / totalTrips : 0;
        return new RouteDemandStats(totalTrips, tripsLast30Days, averageBookedSeats, averageSeatsTotal);
    }

    private Map<Long, Integer> buildConfirmedSeatsByTripId() {
        Map<Long, Integer> seatsByTripId = new HashMap<>();
        try {
            for (Reservation reservation : reservationRepository.findAll()) {
                if (!isConfirmedTripReservation(reservation) || reservation.getTrip() == null || reservation.getTrip().getId() == null) {
                    continue;
                }

                Long tripId = reservation.getTrip().getId();
                int bookedSeats = getReservedPeopleCount(reservation);
                seatsByTripId.put(tripId, seatsByTripId.getOrDefault(tripId, 0) + bookedSeats);
            }
        } catch (IllegalArgumentException e) {
            // Handle legacy database values (e.g., "CANCELED" vs "CANCELLED")
            logger.warn("Error loading reservations due to invalid status enum values", e);
        }
        return seatsByTripId;
    }

    private List<TrainingSample> buildTrainingSamples(List<Trip> historicalTrips,
                                                      Map<Long, Integer> confirmedSeatsByTripId,
                                                      Map<Integer, List<HolidayCalendar>> holidayCache) {
        List<TrainingSample> samples = new ArrayList<>();
        List<TripDemandHistoryItem> history = new ArrayList<>();

        for (Trip trip : historicalTrips) {
            String departure = normalizeLocation(trip.getDeparture());
            String destination = normalizeLocation(trip.getDestination());
            if (departure.isBlank() || destination.isBlank()) {
                continue;
            }

            LocalDate tripDate = trip.getDepartureDateTime().toLocalDate();
            double[] features = buildFeatures(history, departure, destination, tripDate, tripDate, holidayCache, true);
            double bookedSeats = confirmedSeatsByTripId.getOrDefault(trip.getId(), 0);
            samples.add(new TrainingSample(features, bookedSeats));
            history.add(new TripDemandHistoryItem(
                    departure,
                    destination,
                    trip.getDepartureDateTime(),
                    bookedSeats,
                    trip.getSeatsTotal()
            ));
        }

        return samples;
    }

    private double[] buildPredictionFeatures(List<Trip> historicalTrips,
                                             String departure,
                                             String destination,
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             Map<Integer, List<HolidayCalendar>> holidayCache,
                                             boolean includeHolidayFeatures) {
        List<TripDemandHistoryItem> history = new ArrayList<>();
        Map<Long, Integer> confirmedSeatsByTripId = buildConfirmedSeatsByTripId();
        for (Trip trip : historicalTrips) {
            history.add(new TripDemandHistoryItem(
                    normalizeLocation(trip.getDeparture()),
                    normalizeLocation(trip.getDestination()),
                    trip.getDepartureDateTime(),
                    confirmedSeatsByTripId.getOrDefault(trip.getId(), 0),
                    trip.getSeatsTotal()
            ));
        }
        return buildFeatures(history, departure, destination, startDate, endDate, holidayCache, includeHolidayFeatures);
    }

    private double[] buildFeatures(List<TripDemandHistoryItem> history,
                                   String departure,
                                   String destination,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   Map<Integer, List<HolidayCalendar>> holidayCache,
                                   boolean includeHolidayFeatures) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime shortWindow = startDateTime.minusDays(ROUTE_HISTORY_DAYS_SHORT);
        LocalDateTime longWindow = startDateTime.minusDays(ROUTE_HISTORY_DAYS_LONG);

        int routeTripsLast7Days = 0;
        int routeTripsLast30Days = 0;
        double routeSeatsLast7Days = 0;
        double routeSeatsLast30Days = 0;
        int departureTripsLast30Days = 0;
        int destinationTripsLast30Days = 0;

        for (TripDemandHistoryItem item : history) {
            if (!item.departureDateTime().isBefore(startDateTime)) {
                continue;
            }

            boolean sameRoute = item.departure().equals(departure) && item.destination().equals(destination);
            if (sameRoute && !item.departureDateTime().isBefore(shortWindow)) {
                routeTripsLast7Days++;
                routeSeatsLast7Days += item.bookedSeats();
            }
            if (sameRoute && !item.departureDateTime().isBefore(longWindow)) {
                routeTripsLast30Days++;
                routeSeatsLast30Days += item.bookedSeats();
            }
            if (item.departure().equals(departure) && !item.departureDateTime().isBefore(longWindow)) {
                departureTripsLast30Days++;
            }
            if (item.destination().equals(destination) && !item.departureDateTime().isBefore(longWindow)) {
                destinationTripsLast30Days++;
            }
        }

        int dayOfWeek = startDate.getDayOfWeek().getValue();
        int weekOfYear = startDate.get(WeekFields.ISO.weekOfWeekBasedYear());
        int monthValue = startDate.getMonthValue();
        int weekend = isWeekend(startDate.getDayOfWeek()) ? 1 : 0;
        HolidayFeatureData holidayFeatureData = includeHolidayFeatures
                ? buildHolidayFeatures(startDate, endDate, holidayCache)
                : buildNeutralHolidayFeatures();

        return new double[]{
                routeTripsLast7Days,
                routeTripsLast30Days,
                routeSeatsLast7Days,
                routeSeatsLast30Days,
                departureTripsLast30Days,
                destinationTripsLast30Days,
                dayOfWeek,
                weekOfYear,
                monthValue,
                weekend,
                holidayFeatureData.daysUntilNextHoliday(),
                holidayFeatureData.holidaysNext7DaysCount(),
                holidayFeatureData.religiousHolidaysNext7DaysCount(),
                holidayFeatureData.holidayInsideRange(),
                holidayFeatureData.religiousHolidayInsideRange(),
                holidayFeatureData.ramadanPeriod()
        };
    }

    private double[] trainLinearRegression(List<TrainingSample> samples) {
        int featureCount = samples.get(0).features().length;
        int size = featureCount + 1;
        double[][] xtx = new double[size][size];
        double[] xty = new double[size];

        for (TrainingSample sample : samples) {
            double[] row = new double[size];
            row[0] = 1d;
            System.arraycopy(sample.features(), 0, row, 1, featureCount);

            for (int i = 0; i < size; i++) {
                xty[i] += row[i] * sample.target();
                for (int j = 0; j < size; j++) {
                    xtx[i][j] += row[i] * row[j];
                }
            }
        }

        for (int i = 1; i < size; i++) {
            xtx[i][i] += REGULARIZATION;
        }

        return solveLinearSystem(xtx, xty);
    }

    private double predict(double[] coefficients, double[] features) {
        double result = coefficients[0];
        for (int i = 0; i < features.length; i++) {
            result += coefficients[i + 1] * features[i];
        }
        return result;
    }

    private double blendPredictedSeats(double modelPrediction, double routeAverageBookedSeats, int routeTripsCount) {
        if (routeTripsCount <= 1) {
            return routeAverageBookedSeats;
        }

        return ((modelPrediction) + (routeAverageBookedSeats * 2d)) / 3d;
    }

    private double adjustOccupancyRate(double occupancyRate, RouteDemandStats routeDemandStats) {
        double adjustedOccupancyRate = occupancyRate;

        if (routeDemandStats.totalTrips() == 1) {
            adjustedOccupancyRate = Math.min(adjustedOccupancyRate, 0.60d);
        }

        if (routeDemandStats.tripsLast30Days() == 0) {
            adjustedOccupancyRate = adjustedOccupancyRate * 0.75d;
        }

        return clamp(adjustedOccupancyRate, 0, 1d);
    }

    private double estimateReferenceSeats(List<Trip> historicalTrips,
                                          String departure,
                                          String destination,
                                          LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime longWindow = startDateTime.minusDays(ROUTE_HISTORY_DAYS_LONG);
        double seatsTotal = 0;
        int count = 0;

        for (Trip trip : historicalTrips) {
            if (trip.getDepartureDateTime() == null || !trip.getDepartureDateTime().isBefore(startDateTime)) {
                continue;
            }

            if (!normalizeLocation(trip.getDeparture()).equals(departure)
                    || !normalizeLocation(trip.getDestination()).equals(destination)) {
                continue;
            }

            if (trip.getDepartureDateTime().isBefore(longWindow)) {
                continue;
            }

            seatsTotal += trip.getSeatsTotal();
            count++;
        }

        if (count > 0) {
            return seatsTotal / count;
        }

        seatsTotal = 0;
        count = 0;
        for (Trip trip : historicalTrips) {
            if (!normalizeLocation(trip.getDeparture()).equals(departure)
                    || !normalizeLocation(trip.getDestination()).equals(destination)) {
                continue;
            }

            seatsTotal += trip.getSeatsTotal();
            count++;
        }

        if (count > 0) {
            return seatsTotal / count;
        }

        return 3d;
    }

    private double[] solveLinearSystem(double[][] matrix, double[] values) {
        int size = values.length;
        double[][] augmented = new double[size][size + 1];

        for (int i = 0; i < size; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, size);
            augmented[i][size] = values[i];
        }

        for (int pivot = 0; pivot < size; pivot++) {
            int bestRow = pivot;
            for (int row = pivot + 1; row < size; row++) {
                if (Math.abs(augmented[row][pivot]) > Math.abs(augmented[bestRow][pivot])) {
                    bestRow = row;
                }
            }

            double[] temporaryRow = augmented[pivot];
            augmented[pivot] = augmented[bestRow];
            augmented[bestRow] = temporaryRow;

            double pivotValue = augmented[pivot][pivot];
            if (Math.abs(pivotValue) < 1e-9) {
                continue;
            }

            for (int column = pivot; column <= size; column++) {
                augmented[pivot][column] /= pivotValue;
            }

            for (int row = 0; row < size; row++) {
                if (row == pivot) {
                    continue;
                }

                double factor = augmented[row][pivot];
                for (int column = pivot; column <= size; column++) {
                    augmented[row][column] -= factor * augmented[pivot][column];
                }
            }
        }

        double[] solution = new double[size];
        for (int i = 0; i < size; i++) {
            solution[i] = augmented[i][size];
        }
        return solution;
    }

    private boolean isHistoricalTripUsable(Trip trip) {
        if (trip == null || trip.getDepartureDateTime() == null) {
            return false;
        }
        String status = trip.getStatus() == null ? "" : trip.getStatus().trim();
        return !status.equalsIgnoreCase(STATUS_CANCELED) && !status.equalsIgnoreCase(STATUS_CANCELLED);
    }

    private boolean isConfirmedTripReservation(Reservation reservation) {
        if (reservation == null || reservation.getType() != ReservationType.TripReservation) {
            return false;
        }

        ReservationStatus status = reservation.getStatus();
        return status != null 
                && status != ReservationStatus.CANCELLED
                && status != ReservationStatus.PENDING
                && status != ReservationStatus.ACTIVE;
    }

    private int getReservedPeopleCount(Reservation reservation) {
        Integer numberOfPeople = reservation.getNumberOfPeople();
        if (numberOfPeople == null || numberOfPeople < 1) {
            return 1;
        }
        return numberOfPeople;
    }

    private String normalizeLocation(String value) {
        if (value == null) {
            return "";
        }

        String mainPart = value;
        int commaIndex = value.indexOf(',');
        if (commaIndex >= 0) {
            mainPart = value.substring(0, commaIndex);
        }
        return mainPart.trim().toLowerCase(Locale.ROOT);
    }

    private String formatLocation(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String[] parts = value.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private String resolveDemandLevel(double occupancyRate) {
        if (occupancyRate >= 0.75d) {
            return "high";
        }
        if (occupancyRate >= 0.45d) {
            return "medium";
        }
        return "low";
    }

    private String buildWeekLabel(LocalDate date) {
        int weekNumber = date.get(WeekFields.ISO.weekOfWeekBasedYear());
        return "Week " + weekNumber;
    }

    private String buildPassengerAlert(String demandLevel,
                                       String periodLabel,
                                       HolidayEventInfo holidayEventInfo,
                                       boolean holidayBoostActive) {
        if (holidayEventInfo != null && !"high".equalsIgnoreCase(demandLevel)) {
            return "A critical travel holiday falls " + periodLabel + " because "
                    + buildHolidayAlertReason(holidayEventInfo) + ". Demand may increase. Book early.";
        }
        if ("high".equalsIgnoreCase(demandLevel)) {
            if (holidayBoostActive && holidayEventInfo != null) {
                return "Trips on this route " + periodLabel + " may fill quickly because "
                        + buildHolidayAlertReason(holidayEventInfo) + ". Book early.";
            }
            return "Trips on this route " + periodLabel + " may fill quickly. Book early.";
        }
        if ("medium".equalsIgnoreCase(demandLevel)) {
            return "People use this route regularly " + periodLabel + ".";
        }
        return "Demand on this route is lower " + periodLabel + ".";
    }

    private String buildDriverAlert(String demandLevel,
                                    String periodLabel,
                                    HolidayEventInfo holidayEventInfo,
                                    boolean holidayBoostActive,
                                    SuggestedPeriodInfo suggestedPeriodInfo) {
        if (holidayEventInfo != null && !"high".equalsIgnoreCase(demandLevel)) {
            return "Your trip date is close to a critical travel holiday: "
                    + buildHolidayAlertReason(holidayEventInfo)
                    + ". Passenger interest may be higher than usual.";
        }
        if ("high".equalsIgnoreCase(demandLevel)) {
            if (holidayBoostActive && holidayEventInfo != null) {
                return "This route is in high demand " + periodLabel + " because "
                        + buildHolidayAlertReason(holidayEventInfo) + ". Passenger interest may be higher than usual.";
            }
            return "This route is in high demand " + periodLabel + ". Passenger interest may be higher than usual.";
        }
        if (suggestedPeriodInfo != null) {
            return suggestedPeriodInfo.message();
        }
        if ("medium".equalsIgnoreCase(demandLevel)) {
            return "People use this route regularly " + periodLabel + ".";
        }
        return "Demand on this route is lower " + periodLabel + ".";
    }

    private LocalDate resolveEffectiveDate(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null) {
            return dateFrom;
        }
        if (dateTo != null) {
            return dateTo;
        }
        return LocalDate.now();
    }

    private String buildPeriodLabel(LocalDate dateFrom, LocalDate dateTo, LocalDate fallbackDate) {
        LocalDate startDate = dateFrom != null ? dateFrom : (dateTo != null ? dateTo : fallbackDate);
        LocalDate endDate = dateTo != null ? dateTo : startDate;

        if (endDate.isBefore(startDate)) {
            LocalDate temporaryDate = startDate;
            startDate = endDate;
            endDate = temporaryDate;
        }

        if (startDate.equals(endDate)) {
            return "on " + formatAlertDate(startDate);
        }

        return "between " + formatAlertDate(startDate) + " and " + formatAlertDate(endDate);
    }

    private DateRange resolveDateRange(LocalDate dateFrom, LocalDate dateTo, LocalDate fallbackDate) {
        LocalDate startDate = dateFrom != null ? dateFrom : (dateTo != null ? dateTo : fallbackDate);
        LocalDate endDate = dateTo != null ? dateTo : startDate;

        if (endDate.isBefore(startDate)) {
            LocalDate temporaryDate = startDate;
            startDate = endDate;
            endDate = temporaryDate;
        }

        return new DateRange(startDate, endDate);
    }

    private String formatAlertDate(LocalDate date) {
        return date.format(ALERT_DATE_FORMATTER);
    }

    private HolidayFeatureData buildHolidayFeatures(LocalDate startDate,
                                                    LocalDate endDate,
                                                    Map<Integer, List<HolidayCalendar>> holidayCache) {
        LocalDate lookAheadDate = endDate.plusDays(HOLIDAY_LOOKAHEAD_DAYS);
        LocalDate longLookAheadDate = startDate.plusDays(HOLIDAY_MAX_DAYS);
        List<HolidayCalendar> nearbyHolidays = getHolidaysBetween(startDate, lookAheadDate, holidayCache);
        List<HolidayCalendar> longRangeHolidays = getHolidaysBetween(startDate, longLookAheadDate, holidayCache);

        long daysUntilNextHoliday = HOLIDAY_MAX_DAYS;
        int holidaysNext7DaysCount = 0;
        int religiousHolidaysNext7DaysCount = 0;
        int holidayInsideRange = 0;
        int religiousHolidayInsideRange = 0;

        for (HolidayCalendar holidayCalendar : longRangeHolidays) {
            if (holidayCalendar.getHolidayDate().isBefore(startDate)) {
                continue;
            }

            long days = ChronoUnit.DAYS.between(startDate, holidayCalendar.getHolidayDate());
            if (days < daysUntilNextHoliday) {
                daysUntilNextHoliday = days;
            }
        }

        for (HolidayCalendar holidayCalendar : nearbyHolidays) {
            if (!holidayCalendar.getHolidayDate().isBefore(startDate)
                    && !holidayCalendar.getHolidayDate().isAfter(lookAheadDate)) {
                holidaysNext7DaysCount++;
                if (isReligiousHoliday(holidayCalendar)) {
                    religiousHolidaysNext7DaysCount++;
                }
            }

            if (!holidayCalendar.getHolidayDate().isBefore(startDate)
                    && !holidayCalendar.getHolidayDate().isAfter(endDate)) {
                holidayInsideRange = 1;
                if (isReligiousHoliday(holidayCalendar)) {
                    religiousHolidayInsideRange = 1;
                }
            }
        }

        int ramadanPeriod = 0;

        return new HolidayFeatureData(
                Math.min(daysUntilNextHoliday, HOLIDAY_MAX_DAYS),
                holidaysNext7DaysCount,
                religiousHolidaysNext7DaysCount,
                holidayInsideRange,
                religiousHolidayInsideRange,
                ramadanPeriod
        );
    }

    private HolidayFeatureData buildNeutralHolidayFeatures() {
        return new HolidayFeatureData(HOLIDAY_MAX_DAYS, 0, 0, 0, 0, 0);
    }

    private List<HolidayCalendar> getHolidaysBetween(LocalDate startDate,
                                                     LocalDate endDate,
                                                     Map<Integer, List<HolidayCalendar>> holidayCache) {
        List<HolidayCalendar> holidays = new ArrayList<>();
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            for (HolidayCalendar holidayCalendar : getHolidaysForYear(year, holidayCache)) {
                if (!holidayCalendar.getHolidayDate().isBefore(startDate)
                        && !holidayCalendar.getHolidayDate().isAfter(endDate)) {
                    holidays.add(holidayCalendar);
                }
            }
        }
        holidays.sort(Comparator.comparing(HolidayCalendar::getHolidayDate));
        return holidays;
    }

    private List<HolidayCalendar> getHolidaysForYear(int year, Map<Integer, List<HolidayCalendar>> holidayCache) {
        if (!holidayCache.containsKey(year)) {
            holidayCache.put(year, holidayCalendarService.retrieveHolidaysByYear(year));
        }
        return holidayCache.getOrDefault(year, List.of());
    }

    private HolidayEventInfo findRelevantHoliday(LocalDate startDate,
                                                 LocalDate endDate,
                                                 Map<Integer, List<HolidayCalendar>> holidayCache) {
        LocalDate alertStartDate = startDate.minusDays(HOLIDAY_ALERT_WINDOW_DAYS);
        LocalDate alertEndDate = endDate.plusDays(HOLIDAY_ALERT_WINDOW_DAYS);
        List<HolidayCalendar> holidays = getHolidaysBetween(alertStartDate, alertEndDate, holidayCache);
        for (HolidayCalendar holidayCalendar : holidays) {
            boolean criticalForCarpooling = Boolean.TRUE.equals(holidayCalendar.getCriticalForCarpooling());
            long daysFromSelectedRange = daysFromRange(
                    holidayCalendar.getHolidayDate(),
                    startDate,
                    endDate
            );
            logger.info("Holiday candidate evaluated holiday={} date={} category={} criticalForCarpooling={} daysFromSelectedRange={}",
                    holidayCalendar.getHolidayName(),
                    holidayCalendar.getHolidayDate(),
                    holidayCalendar.getHolidayCategory(),
                    criticalForCarpooling,
                    daysFromSelectedRange);
            if (!criticalForCarpooling || daysFromSelectedRange > HOLIDAY_ALERT_WINDOW_DAYS) {
                continue;
            }

            boolean insideRange = !holidayCalendar.getHolidayDate().isBefore(startDate)
                    && !holidayCalendar.getHolidayDate().isAfter(endDate);
            logger.info("Holiday candidate selected holiday={} date={} insideRange={}",
                    holidayCalendar.getHolidayName(),
                    holidayCalendar.getHolidayDate(),
                    insideRange);
            return new HolidayEventInfo(
                    holidayCalendar.getHolidayName(),
                    holidayCalendar.getHolidayDate(),
                    holidayCalendar.getHolidayCategory(),
                    insideRange
            );
        }

        logger.info("No critical holiday selected for range={} to {}", startDate, endDate);
        return null;
    }

    private HolidayEventInfo findClosestCriticalHoliday(LocalDate date,
                                                        Map<Integer, List<HolidayCalendar>> holidayCache) {
        LocalDate startDate = date.minusDays(HOLIDAY_ALERT_WINDOW_DAYS);
        LocalDate endDate = date.plusDays(HOLIDAY_ALERT_WINDOW_DAYS);
        return getHolidaysBetween(startDate, endDate, holidayCache)
                .stream()
                .filter(holiday -> Boolean.TRUE.equals(holiday.getCriticalForCarpooling()))
                .min(Comparator.comparingLong(holiday -> daysFromRange(
                        date,
                        holiday.getHolidayDate(),
                        holiday.getHolidayDate()
                )))
                .map(holiday -> new HolidayEventInfo(
                        holiday.getHolidayName(),
                        holiday.getHolidayDate(),
                        holiday.getHolidayCategory(),
                        holiday.getHolidayDate().equals(date)
                ))
                .orElse(null);
    }

    private long daysFromRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date.isBefore(startDate)) {
            return ChronoUnit.DAYS.between(date, startDate);
        }
        if (date.isAfter(endDate)) {
            return ChronoUnit.DAYS.between(endDate, date);
        }
        return 0;
    }

    private boolean isReligiousHoliday(HolidayCalendar holidayCalendar) {
        return holidayCalendar != null
                && holidayCalendar.getHolidayCategory() != null
                && holidayCalendar.getHolidayCategory().equalsIgnoreCase("religious");
    }

    private String buildHolidayAlertReason(HolidayEventInfo holidayEventInfo) {
        if ("Ramadan Start".equalsIgnoreCase(holidayEventInfo.holidayName())) {
            return "Ramadan starts on " + formatAlertDate(holidayEventInfo.holidayDate());
        }

        return holidayEventInfo.holidayName() + " is on " + formatAlertDate(holidayEventInfo.holidayDate());
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double roundValue(double value) {
        return Math.round(value * 100d) / 100d;
    }

    private record TrainingSample(double[] features, double target) {
    }

    private record TripDemandHistoryItem(String departure,
                                         String destination,
                                         LocalDateTime departureDateTime,
                                         double bookedSeats,
                                         int seatsTotal) {
    }

    private record RouteDemandStats(int totalTrips,
                                    int tripsLast30Days,
                                    double averageBookedSeats,
                                    double averageSeatsTotal) {
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record HolidayFeatureData(double daysUntilNextHoliday,
                                      double holidaysNext7DaysCount,
                                      double religiousHolidaysNext7DaysCount,
                                      double holidayInsideRange,
                                      double religiousHolidayInsideRange,
                                      double ramadanPeriod) {
    }

    private record HolidayEventInfo(String holidayName,
                                    LocalDate holidayDate,
                                    String holidayCategory,
                                    boolean insideRange) {
    }

    private record DemandPredictionResult(double predictedSeats,
                                          double predictedSeatsWithoutHoliday,
                                          double occupancyRate) {
    }

    private record SuggestedPeriodInfo(LocalDate date,
                                       double predictedOccupancyRate,
                                       String message) {
    }
}
