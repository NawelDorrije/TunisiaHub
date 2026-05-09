package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantStatus;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantType;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.ReservationRestaurantRepository;
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
public class ReservationRestaurantService implements IReservationRestaurantService {

    private static final Set<ReservationRestaurantStatus> ACTIVE_RESTAURANT_STATUSES =
            EnumSet.of(ReservationRestaurantStatus.PENDING, ReservationRestaurantStatus.CONFIRMED, ReservationRestaurantStatus.ARRIVED);

    private final ReservationRestaurantRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public List<ReservationRestaurant> retrieveAllReservations() {
        return reservationRepository.findAll();
    }

    @Override
    public ReservationRestaurant retrieveReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    @Override
    public ReservationRestaurant addReservation(ReservationRestaurant reservation) {
        attachCurrentUserIfMissing(reservation);
        normalizeReservation(reservation);
        ReservationRestaurant savedReservation = reservationRepository.save(reservation);
        triggerRequestEmail(savedReservation);
        return savedReservation;
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.delete(getReservationOrThrow(id));
    }

    @Override
    public ReservationRestaurant modifyReservation(ReservationRestaurant reservation) {
        ReservationRestaurantStatus previousStatus = reservation.getId() == null
                ? null
                : reservationRepository.findById(reservation.getId())
                .map(ReservationRestaurant::getStatus)
                .orElse(null);
        normalizeReservation(reservation);
        ReservationRestaurant savedReservation = reservationRepository.save(reservation);
        triggerConfirmationEmailIfNeeded(previousStatus, savedReservation);
        return savedReservation;
    }

    @Override
    public List<ReservationRestaurant> retrieveRestaurantReservations(Long restaurantId, ReservationRestaurantStatus status) {
        if (restaurantId == null) {
            return reservationRepository.findByTypeOrderByIdDesc(ReservationRestaurantType.RestaurantReservation);
        }
        if (status == null) {
            return reservationRepository.findByRestaurant_IdOrderByDateTimeAsc(restaurantId);
        }
        return reservationRepository.findByRestaurant_IdAndStatusOrderByDateTimeAsc(restaurantId, status);
    }

    @Override
    public List<ReservationRestaurant> retrieveReservationsByUser(Long userId) {
        if (userId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        return reservationRepository.findByUser_IdOrderByDateTimeDesc(userId);
    }

    @Override
    public List<ReservationRestaurant> retrieveMyReservations() {
        User currentUser = resolveCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }
        return reservationRepository.findByUser_IdOrderByDateTimeDesc(currentUser.getId());
    }

    @Override
    public ReservationRestaurant confirmRestaurantReservation(Long reservationId, List<Long> tableIds) {
        ReservationRestaurant reservation = getReservationOrThrow(reservationId);
        ReservationRestaurantStatus previousStatus = reservation.getStatus();
        validateRestaurantReservation(reservation);

        List<Long> effectiveTableIds = resolveEffectiveTableIds(reservation, tableIds);
        if (effectiveTableIds.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one table must be assigned");
        }

        List<RestaurantTable> assignedTables = loadAndValidateTables(reservation, effectiveTableIds, reservation.getId());
        validatePartySizeCoverage(reservation, assignedTables);

        reservation.setTables(assignedTables);
        reservation.setTablePreSelected(true);
        reservation.setStatus(ReservationRestaurantStatus.CONFIRMED);
        reservation.setLastTableAssignedBy(resolveCurrentUser());
        ReservationRestaurant savedReservation = reservationRepository.save(reservation);
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
                ReservationRestaurantType.RestaurantReservation,
                dateTime,
                ACTIVE_RESTAURANT_STATUSES,
                List.of(tableId),
                excludedReservationId
        ).isEmpty();
    }

    @Override
    public ReservationRestaurant cancelReservation(Long reservationId) {
        ReservationRestaurant reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationRestaurantStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Override
    public ReservationRestaurant completeReservation(Long reservationId) {
        ReservationRestaurant reservation = getReservationOrThrow(reservationId);
        reservation.setStatus(ReservationRestaurantStatus.COMPLETED);
        return reservationRepository.save(reservation);
    }

    @Override
    public ReservationRestaurant checkInReservation(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "check-in token is required");
        }

        ReservationRestaurant reservation = reservationRepository.findByCheckInToken(token.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found for this check-in token"));

        if (reservation.getStatus() != ReservationRestaurantStatus.CONFIRMED && reservation.getStatus() != ReservationRestaurantStatus.ARRIVED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only confirmed reservations can be checked in");
        }

        if (reservation.getStatus() != ReservationRestaurantStatus.ARRIVED) {
            reservation.setStatus(ReservationRestaurantStatus.ARRIVED);
            reservation.setCheckedInAt(LocalDateTime.now());
        } else if (reservation.getCheckedInAt() == null) {
            reservation.setCheckedInAt(LocalDateTime.now());
        }

        return reservationRepository.save(reservation);
    }

    private void normalizeReservation(ReservationRestaurant reservation) {
        if (reservation == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation payload is required");
        }

        ReservationRestaurant existingReservation = reservation.getId() == null
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

        if (reservation.getType() == ReservationRestaurantType.RestaurantReservation) {
            normalizeRestaurantReservation(reservation);
        } else if (reservation.getType() == null && reservation.getRestaurant() != null) {
            reservation.setType(ReservationRestaurantType.RestaurantReservation);
            normalizeRestaurantReservation(reservation);
        }
    }

    private void normalizeRestaurantReservation(ReservationRestaurant reservation) {
        if (reservation.getStatus() == null) {
            reservation.setStatus(ReservationRestaurantStatus.PENDING);
        }

        ReservationRestaurant existingReservation = reservation.getId() == null
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

        if (reservation.getStatus() == ReservationRestaurantStatus.CONFIRMED && reservation.getTables().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A confirmed restaurant reservation must have at least one table");
        }
    }

    private ReservationRestaurant getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    private void validateRestaurantReservation(ReservationRestaurant reservation) {
        if (reservation.getType() != ReservationRestaurantType.RestaurantReservation) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This action is only supported for restaurant reservations");
        }
        if (reservation.getRestaurant() == null || reservation.getRestaurant().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation must reference a restaurant");
        }
    }

    private List<Long> resolveEffectiveTableIds(ReservationRestaurant reservation, List<Long> requestedTableIds) {
        if (requestedTableIds != null && !requestedTableIds.isEmpty()) {
            return requestedTableIds.stream().filter(Objects::nonNull).distinct().toList();
        }
        return extractTableIds(reservation);
    }

    private List<Long> extractTableIds(ReservationRestaurant reservation) {
        if (reservation.getTables() == null || reservation.getTables().isEmpty()) {
            return List.of();
        }
        return reservation.getTables().stream()
                .map(RestaurantTable::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private void validatePartySizeCoverage(ReservationRestaurant reservation, List<RestaurantTable> tables) {
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

    private List<RestaurantTable> loadAndValidateTables(ReservationRestaurant reservation, List<Long> tableIds, Long excludedReservationId) {
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

        List<ReservationRestaurant> conflicts = reservationRepository.findTableConflicts(
                restaurantId,
                ReservationRestaurantType.RestaurantReservation,
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
    private void attachCurrentUserIfMissing(ReservationRestaurant reservation) {
        if (reservation.getUser() != null && reservation.getUser().getId() != null) {
            return;
        }
        User user = resolveCurrentUser();
        if (user != null) {
            reservation.setUser(user);
        }
    }

    private void triggerRequestEmail(ReservationRestaurant reservation) {
        if (reservation.getId() != null) {
            reservationRepository.findDetailedById(reservation.getId())
                .ifPresent(emailService::sendReservationRequest);
        }
    }

    private void triggerConfirmationEmailIfNeeded(ReservationRestaurantStatus previousStatus, ReservationRestaurant reservation) {
        if (previousStatus == ReservationRestaurantStatus.PENDING && reservation.getStatus() == ReservationRestaurantStatus.CONFIRMED) {
            if (reservation.getId() != null) {
                reservationRepository.findDetailedById(reservation.getId())
                    .ifPresent(emailService::sendReservationConfirmation);
            }
        }
    }

    private void ensureCheckInToken(ReservationRestaurant reservation) {
        if (!StringUtils.hasText(reservation.getCheckInToken())) {
            reservation.setCheckInToken(UUID.randomUUID().toString());
        }
    }
}
