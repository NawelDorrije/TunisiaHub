package org.example.backend_tunisiahub.Services.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantRepository;
import org.example.backend_tunisiahub.Repositories.Restaurant.RestaurantTableRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTableService implements IRestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    public List<RestaurantTable> retrieveAllTables() {
        return restaurantTableRepository.findAll();
    }

    @Override
    public RestaurantTable retrieveTable(Long id) {
        return restaurantTableRepository.findById(id).orElse(null);
    }

    @Override
    public List<RestaurantTable> retrieveTablesByRestaurant(Long restaurantId, TableStatus status) {
        if (status == null) {
            return restaurantTableRepository.findByRestaurant_Id(restaurantId);
        }
        return restaurantTableRepository.findByRestaurant_IdAndStatus(restaurantId, status);
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
        restaurantTableRepository.deleteById(id);
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
