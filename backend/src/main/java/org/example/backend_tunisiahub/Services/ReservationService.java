package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantTableRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class ReservationService implements IReservationService {

    private static final Set<ReservationStatus> ACTIVE_RESTAURANT_STATUSES =
            EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.ARRIVED);

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public List<Reservation> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public Reservation retrieveReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public Reservation addReservation(Reservation reservation) {
        attachCurrentUserIfMissing(reservation);
        normalizeReservation(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        triggerRequestEmail(savedReservation);
        return savedReservation;
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.delete(getReservationOrThrow(id));
    }

    @Override
    public Reservation modifyReservation(Reservation reservation) {
        ReservationStatus previousStatus = reservation.getId() == null
                ? null
                : reservationRepository.findById(reservation.getId())
                .map(Reservation::getStatus)
                .orElse(null);
        normalizeReservation(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        triggerConfirmationEmailIfNeeded(previousStatus, savedReservation);
        return savedReservation;
    }

    @Override
    public List<Reservation> retrieveRestaurantReservations(Long restaurantId, ReservationStatus status) {
        if (restaurantId == null) {
            return reservationRepository.findByTypeOrderByIdDesc(ReservationType.RestaurantReservation);
        }
        if (status == null) {
            return reservationRepository.findByRestaurant_IdOrderByDateTimeAsc(restaurantId);
        }
        return reservationRepository.findByRestaurant_IdAndStatusOrderByDateTimeAsc(restaurantId, status);
    }

    @Override
    public List<Reservation> retrieveReservationsByUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        return reservationRepository.findByUser_IdOrderByDateTimeDesc(userId);
    }

    @Override
    public List<Reservation> retrieveMyReservations() {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return reservationRepository.findByUser_IdOrderByDateTimeDesc(currentUser.getId());
    }

    @Override
    public Reservation confirmRestaurantReservation(Long reservationId, List<Long> tableIds) {
        Reservation reservation = getReservationOrThrow(reservationId);
        ReservationStatus previousStatus = reservation.getStatus();
        validateRestaurantReservation(reservation);

        List<Long> effectiveTableIds = resolveEffectiveTableIds(reservation, tableIds);
        if (effectiveTableIds.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one table must be assigned");
        }

        List<RestaurantTable> assignedTables = loadAndValidateTables(reservation, effectiveTableIds, reservation.getId());
        validatePartySizeCoverage(reservation, assignedTables);

        reservation.setTables(assignedTables);
        reservation.setTablePreSelected(true);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setLastTableAssignedBy(resolveCurrentUser());
        Reservation savedReservation = reservationRepository.save(reservation);
        triggerConfirmationEmailIfNeeded(previousStatus, savedReservation);
        return savedReservation;
    }

    @Override
    public boolean isTableAvailableForReservation(Long restaurantId, Long tableId, LocalDateTime dateTime, Long excludedReservationId) {
        if (restaurantId == null || tableId == null || dateTime == null) {
            return false;
        }

        return reservationRepository.findTableConflicts(
                restaurantId,
                ReservationType.RestaurantReservation,
                dateTime,
                ACTIVE_RESTAURANT_STATUSES,
                List.of(tableId),
                excludedReservationId
        ).isEmpty();
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationStatus.COMPLETED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation checkInReservation(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "check-in token is required");
        }

        Reservation reservation = reservationRepository.findByCheckInToken(token.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found for this check-in token"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.ARRIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only confirmed reservations can be checked in");
        }

        if (reservation.getStatus() != ReservationStatus.ARRIVED) {
            reservation.setStatus(ReservationStatus.ARRIVED);
            reservation.setCheckedInAt(LocalDateTime.now());
        } else if (reservation.getCheckedInAt() == null) {
            reservation.setCheckedInAt(LocalDateTime.now());
        }

        return reservationRepository.save(reservation);
    }

    private void normalizeReservation(Reservation reservation) {
        if (reservation == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation payload is required");
        }

        Reservation existingReservation = reservation.getId() == null
                ? null
                : reservationRepository.findById(reservation.getId()).orElse(null);

        if (reservation.getUser() == null && existingReservation != null) {
            reservation.setUser(existingReservation.getUser());
        }
        if (!StringUtils.hasText(reservation.getCheckInToken()) && existingReservation != null) {
            reservation.setCheckInToken(existingReservation.getCheckInToken());
        }
        if (reservation.getCheckedInAt() == null && existingReservation != null) {
            reservation.setCheckedInAt(existingReservation.getCheckedInAt());
        }
        ensureCheckInToken(reservation);

        if (reservation.getType() == ReservationType.RestaurantReservation) {
            normalizeRestaurantReservation(reservation);
        } else if (reservation.getType() == null && reservation.getRestaurant() != null) {
            reservation.setType(ReservationType.RestaurantReservation);
            normalizeRestaurantReservation(reservation);
        }
    }

    private void normalizeRestaurantReservation(Reservation reservation) {
        if (reservation.getStatus() == null) {
            reservation.setStatus(ReservationStatus.PENDING);
        }

        Reservation existingReservation = reservation.getId() == null
                ? null
                : reservationRepository.findById(reservation.getId()).orElse(null);

        if (reservation.getRestaurant() == null || reservation.getRestaurant().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation must reference a restaurant");
        }

        Restaurant restaurant = restaurantRepository.findById(reservation.getRestaurant().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        reservation.setRestaurant(restaurant);

        List<Long> selectedTableIds = reservation.getTables() == null
                ? extractTableIds(existingReservation)
                : extractTableIds(reservation);

        if (!selectedTableIds.isEmpty()) {
            List<RestaurantTable> selectedTables = loadAndValidateTables(reservation, selectedTableIds, reservation.getId());
            validatePartySizeCoverage(reservation, selectedTables);
            reservation.setTables(selectedTables);
            reservation.setTablePreSelected(true);
            User currentUser = resolveCurrentUser();
            reservation.setLastTableAssignedBy(currentUser != null
                    ? currentUser
                    : existingReservation == null ? null : existingReservation.getLastTableAssignedBy());
        } else {
            reservation.setTables(new ArrayList<>());
            reservation.setTablePreSelected(false);
            if (existingReservation != null && reservation.getLastTableAssignedBy() == null) {
                reservation.setLastTableAssignedBy(existingReservation.getLastTableAssignedBy());
            }
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED && reservation.getTables().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A confirmed restaurant reservation must have at least one table");
        }
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    private void validateRestaurantReservation(Reservation reservation) {
        if (reservation.getType() != ReservationType.RestaurantReservation) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This action is only supported for restaurant reservations");
        }
        if (reservation.getRestaurant() == null || reservation.getRestaurant().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation must reference a restaurant");
        }
    }

    private List<Long> resolveEffectiveTableIds(Reservation reservation, List<Long> requestedTableIds) {
        if (requestedTableIds != null && !requestedTableIds.isEmpty()) {
            return requestedTableIds.stream().filter(Objects::nonNull).distinct().toList();
        }
        return extractTableIds(reservation);
    }

    private List<Long> extractTableIds(Reservation reservation) {
        if (reservation.getTables() == null || reservation.getTables().isEmpty()) {
            return List.of();
        }
        return reservation.getTables().stream()
                .map(RestaurantTable::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void validatePartySizeCoverage(Reservation reservation, List<RestaurantTable> tables) {
        if (reservation.getPartySize() == null) {
            return;
        }

        int totalCapacity = tables.stream()
                .map(RestaurantTable::getCapacity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (totalCapacity < reservation.getPartySize()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Selected tables do not cover the requested party size");
        }
    }

    private List<RestaurantTable> loadAndValidateTables(Reservation reservation, List<Long> tableIds, Long excludedReservationId) {
        validateRestaurantReservation(reservation);
        if (reservation.getDateTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation dateTime is required");
        }

        List<RestaurantTable> tables = new ArrayList<>(restaurantTableRepository.findAllById(tableIds));
        if (tables.size() != tableIds.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "One or more tables were not found");
        }

        Long restaurantId = reservation.getRestaurant().getId();
        for (RestaurantTable table : tables) {
            if (table.getRestaurant() == null || !restaurantId.equals(table.getRestaurant().getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Selected tables must belong to the reservation restaurant");
            }
            if (!table.isActive()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "One or more selected tables are no longer available");
            }
            if (table.getStatus() == TableStatus.OCCUPIED) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "One or more tables are currently not selectable");
            }
        }

        List<Reservation> conflicts = reservationRepository.findTableConflicts(
                restaurantId,
                ReservationType.RestaurantReservation,
                reservation.getDateTime(),
                ACTIVE_RESTAURANT_STATUSES,
                tableIds,
                excludedReservationId
        );
        if (!conflicts.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "One or more selected tables are no longer available for that time");
        }

        return tables;
    }

    private User resolveCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName());
    }

    /** Links the reservation to the logged-in user when creating a booking (client JWT). */
    private void attachCurrentUserIfMissing(Reservation reservation) {
        if (reservation.getUser() != null && reservation.getUser().getId() != null) {
            return;
        }
        User user = resolveCurrentUser();
        if (user != null) {
            reservation.setUser(user);
        }
    }

    private void triggerRequestEmail(Reservation reservation) {
        if (reservation.getId() != null) {
            reservationRepository.findDetailedById(reservation.getId())
                .ifPresent(emailService::sendReservationRequest);
        }
    }

    private void triggerConfirmationEmailIfNeeded(ReservationStatus previousStatus, Reservation reservation) {
        if (previousStatus == ReservationStatus.PENDING && reservation.getStatus() == ReservationStatus.CONFIRMED) {
            if (reservation.getId() != null) {
                reservationRepository.findDetailedById(reservation.getId())
                    .ifPresent(emailService::sendReservationConfirmation);
            }
        }
    }

    private void ensureCheckInToken(Reservation reservation) {
        if (!StringUtils.hasText(reservation.getCheckInToken())) {
            reservation.setCheckInToken(UUID.randomUUID().toString());
        }
    }
}
