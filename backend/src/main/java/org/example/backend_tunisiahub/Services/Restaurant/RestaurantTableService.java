package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantTableRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RestaurantTableService implements IRestaurantTableService {

    private static final Set<ReservationStatus> ACTIVE_RESTAURANT_STATUSES =
            EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.ARRIVED);

    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<RestaurantTable> retrieveAllTables() {
        return restaurantTableRepository.findAll();
    }

    @Override
    public RestaurantTable retrieveTable(Long id) {
        return restaurantTableRepository.findById(id).orElse(null);
    }

    @Override
    public List<RestaurantTable> retrieveTablesByRestaurant(Long restaurantId, TableStatus status, LocalDateTime dateTime, Integer partySize) {
        List<RestaurantTable> tables;
        if (status == null) {
            tables = restaurantTableRepository.findByRestaurant_IdAndActiveTrue(restaurantId);
        } else {
            tables = restaurantTableRepository.findByRestaurant_IdAndStatusAndActiveTrue(restaurantId, status);
        }

        if (dateTime != null) {
            // Check conflicts for all these tables at once
            List<Long> tableIds = tables.stream().map(RestaurantTable::getId).toList();
            List<org.example.backend_tunisiahub.Entities.Reservation> conflicts = reservationRepository.findTableConflicts(
                    restaurantId,
                    ReservationType.RestaurantReservation,
                    dateTime,
                    ACTIVE_RESTAURANT_STATUSES,
                    tableIds,
                    null
            );

            Set<Long> busyTableIds = new java.util.HashSet<>();
            for (org.example.backend_tunisiahub.Entities.Reservation r : conflicts) {
                if (r.getTables() != null) {
                    for (RestaurantTable t : r.getTables()) {
                        busyTableIds.add(t.getId());
                    }
                }
            }

            for (RestaurantTable table : tables) {
                boolean available = !busyTableIds.contains(table.getId()) && table.getStatus() != TableStatus.OCCUPIED;
                if (partySize != null && table.getCapacity() < partySize) {
                    // For client picking, we might still want to show it but as "not suitable"
                    // In the current frontend logic, capacity < partySize falls through to Gray color
                }
                table.setAvailable(available);
            }
        }
        
        return tables;
    }

    @Override
    public RestaurantTable addTable(RestaurantTable table) {
        normalizeTable(table);
        return restaurantTableRepository.save(table);
    }

    @Override
    public RestaurantTable modifyTable(RestaurantTable table) {
        if (table.getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Table id is required for update");
        }
        normalizeTable(table);
        return restaurantTableRepository.save(table);
    }

    @Override
    public void deleteTable(Long id) {
        RestaurantTable table = restaurantTableRepository.findById(id).orElse(null);
        if (table != null) {
            table.setActive(false);
            restaurantTableRepository.save(table);
        }
    }

    @Override
    public void saveFloorPlan(Long restaurantId, List<RestaurantTable> tables) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));

        // Get existing active tables to determine which to deactivate
        List<RestaurantTable> existingTables = restaurantTableRepository.findByRestaurant_IdAndActiveTrue(restaurantId);
        
        // Mark tables as inactive if they are NOT in the incoming list
        existingTables.stream()
                .filter(existing -> tables.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())))
                .forEach(existing -> {
                    existing.setActive(false);
                    restaurantTableRepository.save(existing);
                });

        // Save or Update incoming tables
        for (RestaurantTable table : tables) {
            table.setRestaurant(restaurant);
            table.setActive(true); // Ensure re-activated if ID matched (though usually IDs are new)
            if (table.getStatus() == null) {
                table.setStatus(TableStatus.AVAILABLE);
            }
            restaurantTableRepository.save(table);
        }
    }

    private void normalizeTable(RestaurantTable table) {
        if (table == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Table payload is required");
        }
        if (table.getRestaurant() == null || table.getRestaurant().getId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Table must reference a restaurant");
        }
        Restaurant restaurant = restaurantRepository.findById(table.getRestaurant().getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        table.setRestaurant(restaurant);
        if (table.getStatus() == null) {
            table.setStatus(TableStatus.AVAILABLE);
        }
    }
}
