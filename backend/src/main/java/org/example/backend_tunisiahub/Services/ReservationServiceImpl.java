package org.example.backend_tunisiahub.Services;

import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ReservationDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.ReservationMapper;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Camping.ActivityRepository;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.Services.Camping.Pricing.DynamicPricingService;
import org.example.backend_tunisiahub.Services.Camping.Pricing.PricingAuditService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ICampingReservationService {

    private final ReservationRepository reservationRepository;
    private final SpotRepository        spotRepository;
    private final UserRepository        userRepository;
    private final ActivityRepository    activityRepository;
    private final ReservationMapper     reservationMapper;
    private final DynamicPricingService dynamicPricingService; // ← ADD THIS
    private final PricingAuditService pricingAuditService;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  SpotRepository spotRepository,
                                  UserRepository userRepository,
                                  ActivityRepository activityRepository,
                                  ReservationMapper reservationMapper,
                                  DynamicPricingService dynamicPricingService,
                                  PricingAuditService pricingAuditService) {
        this.reservationRepository = reservationRepository;
        this.spotRepository        = spotRepository;
        this.userRepository        = userRepository;
        this.activityRepository    = activityRepository;
        this.reservationMapper     = reservationMapper;
        this.dynamicPricingService = dynamicPricingService;
        this.pricingAuditService = pricingAuditService;// ← ADD THIS

    }

    // ── createReservation ─────────────────────────────────────────────────────

    @Override
    public ReservationDTO createReservation(ReservationDTO dto) {

        // 1. Validate dates
        if (!dto.getCheckOut().isAfter(dto.getCheckIn())) {
            throw new RuntimeException("checkOut must be after checkIn");
        }

        // 2. Load spot
        Spot spot = spotRepository.findById(dto.getSpotId())
                .orElseThrow(() -> new RuntimeException("Spot not found: " + dto.getSpotId()));

        // 3. Check spot is active (soft-deleted guard)
        if (!spot.getActive()) {
            throw new RuntimeException("Spot is not active");
        }

        // ── NOTE: SpotStatus (LIBRE / OCCUPE) is NOT checked here. ───────────
        // SpotStatus reflects which guest is *currently* on-site today.
        // It must not block future reservations whose dates don't overlap.
        // Real availability is enforced by the date-overlap query below (step 5).

        // 4. Check capacity
        if (dto.getNumberOfGuests() > spot.getCapacity()) {
            throw new RuntimeException(
                    "Number of guests exceeds spot capacity of " + spot.getCapacity());
        }

        // 5. Check for date conflicts — the only true availability guard
        if (reservationRepository.existsOverlappingReservation(
                spot.getId(), dto.getCheckIn(), dto.getCheckOut())) {
            throw new RuntimeException(
                    "Spot is already reserved for the selected dates");
        }

        // 6. Load user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getUserId()));

        // 7. Load activities (optional)
        List<Activity> activities = List.of();
        if (dto.getActivityIds() != null && !dto.getActivityIds().isEmpty()) {
            activities = activityRepository.findAllById(dto.getActivityIds());
        }

        // 8. Calculate total price
        long nights = ChronoUnit.DAYS.between(dto.getCheckIn(), dto.getCheckOut());

        // Resolve the effective price for the check-in date.
        // Uses today's cached dynamicPrice if already computed,
        // or triggers an on-the-fly computation (without saving) if not.
        BigDecimal effectivePricePerNight = dynamicPricingService
                .getEffectivePrice(spot, dto.getCheckIn());

        BigDecimal spotTotal = effectivePricePerNight
                .multiply(BigDecimal.valueOf(nights));

        BigDecimal activitiesTotal = activities.stream()
                .map(Activity::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPrice = spotTotal.add(activitiesTotal);

        // ── Step 9 — Build and save reservation ──────────────────────────────
        Reservation reservation = Reservation.builder()
                .user(user)
                .spot(spot)
                .activities(activities)
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .numberOfGuests(dto.getNumberOfGuests())
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .type(ReservationType.CampingReservation)
                .notes(dto.getNotes())
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

// ── Step 10 — AI feedback loop (VERY IMPORTANT) ──────────────────────
// Notify AI system that this pricing successfully led to a booking
        pricingAuditService.markBookingConfirmed(spot.getId());

        return reservationMapper.toDTO(savedReservation);
    }

    // ── updateStatus ──────────────────────────────────────────────────────────

    @Override
    public ReservationDTO updateStatus(Long id, ReservationStatus newStatus) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));

        reservation.setStatus(newStatus);
        reservation.setUpdatedAt(LocalDateTime.now());

        // Sync SpotStatus only for the *current* stay (not future bookings).
        // OCCUPE  → guest is physically on-site right now.
        // LIBRE   → spot is no longer occupied today (checkout / cancellation).
        Spot spot = reservation.getSpot();
        if (newStatus == ReservationStatus.ACTIVE) {
            // Guest has physically checked in — mark spot as occupied TODAY
            spot.setStatus(SpotStatus.OCCUPE);
            spotRepository.save(spot);
        } else if (newStatus == ReservationStatus.COMPLETED
                || newStatus == ReservationStatus.CANCELLED) {
            // Guest has left or booking was cancelled — free the spot for today
            spot.setStatus(SpotStatus.LIBRE);
            spotRepository.save(spot);
        }
        // PENDING → CONFIRMED → PAID: spot not yet occupied; do not change SpotStatus.

        return reservationMapper.toDTO(reservationRepository.save(reservation));
    }

    // ── cancelReservation ─────────────────────────────────────────────────────

    @Override
    public void cancelReservation(Long id) {
        updateStatus(id, ReservationStatus.CANCELLED);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Override
    public ReservationDTO getById(Long id) {
        return reservationRepository.findById(id)
                .map(reservationMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + id));
    }

    @Override
    public List<ReservationDTO> getAll() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getByUser(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getBySpot(Long spotId) {
        return reservationRepository.findBySpotId(spotId).stream()
                .map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(reservationMapper::toDTO).collect(Collectors.toList());
    }
}
