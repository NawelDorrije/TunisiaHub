package org.example.backend_tunisiahub.Controllers.Restaurant;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Restaurant.Restaurant;
import org.example.backend_tunisiahub.Entities.Restaurant.RestaurantTable;
import org.example.backend_tunisiahub.Entities.Restaurant.TableStatus;
import org.example.backend_tunisiahub.Services.Restaurant.IRestaurantTableService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-tables")
@RequiredArgsConstructor
public class RestaurantTableController {

    private final IRestaurantTableService restaurantTableService;

    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return restaurantTableService.retrieveAllTables();
    }

    @GetMapping("/get/{id}")
    public RestaurantTable getTableById(@PathVariable Long id) {
        return restaurantTableService.retrieveTable(id);
    }

    @GetMapping("/by-restaurant/{restaurantId}")
    public List<RestaurantTable> getTablesByRestaurant(@PathVariable Long restaurantId,
                                                       @RequestParam(required = false) TableStatus status) {
        return restaurantTableService.retrieveTablesByRestaurant(restaurantId, status);
    }

    @GetMapping("/statuses")
    public TableStatus[] getTableStatuses() {
        return TableStatus.values();
    }

    @PostMapping("/add")
    public RestaurantTable createTable(@RequestBody RestaurantTableWriteRequest request) {
        RestaurantTable table = toEntity(request);
        return restaurantTableService.addTable(table);
    }

    @PutMapping("/update")
    public RestaurantTable updateTable(@RequestBody RestaurantTableUpdateRequest request) {
        RestaurantTable table = toEntity(
                request.tableNumber(),
                request.capacity(),
                request.location(),
                request.status(),
                request.restaurantId()
        );
        return restaurantTableService.modifyTable(table);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteTable(@PathVariable Long id) {
        restaurantTableService.deleteTable(id);
    }

    private RestaurantTable toEntity(RestaurantTableWriteRequest request) {
        return toEntity(
                request.tableNumber(),
                request.capacity(),
                request.location(),
                request.status(),
                request.restaurantId()
        );
    }

    private RestaurantTable toEntity(Integer tableNumber,
                                     Integer capacity,
                                     String location,
                                     TableStatus status,
                                     Long restaurantId) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setLocation(location);
        table.setStatus(status);
        if (restaurantId != null) {
            Restaurant restaurant = new Restaurant();
            restaurant.setId(restaurantId);
            table.setRestaurant(restaurant);
        }
        return table;
    }

    private record RestaurantTableWriteRequest(
            Integer tableNumber,
            Integer capacity,
            String location,
            TableStatus status,
            Long restaurantId
    ) {
    }

    private record RestaurantTableUpdateRequest(
            Long id,
            Integer tableNumber,
            Integer capacity,
            String location,
            TableStatus status,
            Long restaurantId
    ) {}
}
