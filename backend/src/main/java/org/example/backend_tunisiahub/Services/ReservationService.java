package org.example.backend_tunisiahub.Services;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final UserRepository userRepository;

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
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationOrThrow(id);
        releaseReservationTables(reservation);
        reservationRepository.delete(reservation);
    }

    @Override
    public Reservation modifyReservation(Reservation reservation) {
        normalizeReservation(reservation);
        return reservationRepository.save(reservation);
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
    public Reservation confirmRestaurantReservation(Long reservationId, List<Long> tableIds) {
        Reservation reservation = getReservationOrThrow(reservationId);
        validateRestaurantReservation(reservation);
        if (tableIds == null || tableIds.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "At least one table must be assigned");
        }

        releaseReservationTables(reservation);

        List<RestaurantTable> assignedTables = loadAndValidateTables(reservation, tableIds);
        int totalCapacity = assignedTables.stream()
                .map(RestaurantTable::getCapacity)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();

        if (reservation.getPartySize() != null && totalCapacity < reservation.getPartySize()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned tables do not cover the requested party size");
        }

        assignedTables.forEach(table -> table.setStatus(TableStatus.RESERVED));
        restaurantTableRepository.saveAll(assignedTables);

        reservation.setTables(assignedTables);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        releaseReservationTables(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation completeReservation(Long reservationId) {
        Reservation reservation = getReservationOrThrow(reservationId);
        releaseReservationTables(reservation);
        reservation.setStatus(ReservationStatus.COMPLETED);
        return reservationRepository.save(reservation);
    }

    private void normalizeReservation(Reservation reservation) {
        if (reservation == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reservation payload is required");
        }

        if (reservation.getType() == ReservationType.RestaurantReservation) {
            if (reservation.getStatus() == null) {
                reservation.setStatus(ReservationStatus.PENDING);
            }
            if (reservation.getRestaurant() != null && reservation.getRestaurant().getId() != null) {
                Restaurant restaurant = restaurantRepository.findById(reservation.getRestaurant().getId())
                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));
                reservation.setRestaurant(restaurant);
            }
            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                reservation.setTables(new ArrayList<>());
            } else if (reservation.getTables() != null && !reservation.getTables().isEmpty()) {
                List<Long> tableIds = reservation.getTables().stream()
                        .map(RestaurantTable::getId)
                        .filter(Objects::nonNull)
                        .toList();
                reservation.setTables(loadAndValidateTables(reservation, tableIds));
            }
        } else if (reservation.getType() == null && reservation.getRestaurant() != null) {
            reservation.setType(ReservationType.RestaurantReservation);
            normalizeReservation(reservation);
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

    private List<RestaurantTable> loadAndValidateTables(Reservation reservation, List<Long> tableIds) {
        validateRestaurantReservation(reservation);
        List<RestaurantTable> tables = new ArrayList<>(restaurantTableRepository.findAllById(tableIds));
        if (tables.size() != tableIds.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "One or more tables were not found");
        }

        Long restaurantId = reservation.getRestaurant().getId();
        for (RestaurantTable table : tables) {
            if (table.getRestaurant() == null || !restaurantId.equals(table.getRestaurant().getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned tables must belong to the reservation restaurant");
            }
            if (table.getStatus() != TableStatus.AVAILABLE) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Assigned tables must be AVAILABLE");
            }
        }
        return tables;
    }

    private void releaseReservationTables(Reservation reservation) {
        if (reservation.getTables() == null || reservation.getTables().isEmpty()) {
            return;
        }
        reservation.getTables().forEach(table -> table.setStatus(TableStatus.AVAILABLE));
        restaurantTableRepository.saveAll(reservation.getTables());
        reservation.setTables(new ArrayList<>());
    }

    /** Links the reservation to the logged-in user when creating a booking (client JWT). */
    private void attachCurrentUserIfMissing(Reservation reservation) {
        if (reservation.getUser() != null && reservation.getUser().getId() != null) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return;
        }
        User user = userRepository.findByEmail(auth.getName());
        if (user != null) {
            reservation.setUser(user);
        }
    }
}
