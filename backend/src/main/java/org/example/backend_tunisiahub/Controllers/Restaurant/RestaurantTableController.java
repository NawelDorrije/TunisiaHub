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
                                                       @RequestParam(required = false) TableStatus status,
                                                       @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime dateTime,
                                                       @RequestParam(required = false) Integer partySize) {
        return restaurantTableService.retrieveTablesByRestaurant(restaurantId, status, dateTime, partySize);
    }

    @GetMapping("/statuses")
    public TableStatus[] getTableStatuses() {
        return TableStatus.values();
    }

    @PostMapping("/add")
    public RestaurantTable createTable(@RequestBody RestaurantTableWriteRequest request) {
        return restaurantTableService.addTable(toEntity(request));
    }

    @PutMapping("/update")
    public RestaurantTable updateTable(@RequestBody RestaurantTableUpdateRequest request) {
        return restaurantTableService.modifyTable(toEntity(request));
    }

    @DeleteMapping("/delete/{id}")
    public void deleteTable(@PathVariable Long id) {
        restaurantTableService.deleteTable(id);
    }

    @PutMapping("/bulk/{restaurantId}")
    public void saveFloorPlan(@PathVariable Long restaurantId, @RequestBody List<RestaurantTableUpdateRequest> requests) {
        List<RestaurantTable> tables = requests.stream()
                .map(this::toEntity)
                .toList();
        restaurantTableService.saveFloorPlan(restaurantId, tables);
    }

    private RestaurantTable toEntity(RestaurantTableWriteRequest request) {
        return toEntity(
                null,
                request.tableNumber(),
                request.capacity(),
                request.location(),
                request.status(),
                request.restaurantId(),
                request.x(),
                request.y(),
                request.width(),
                request.height(),
                request.rotation(),
                request.shapeType(),
                request.label(),
                request.color()
        );
    }

    private RestaurantTable toEntity(RestaurantTableUpdateRequest request) {
        return toEntity(
                request.id(),
                request.tableNumber(),
                request.capacity(),
                request.location(),
                request.status(),
                request.restaurantId(),
                request.x(),
                request.y(),
                request.width(),
                request.height(),
                request.rotation(),
                request.shapeType(),
                request.label(),
                request.color()
        );
    }

    private RestaurantTable toEntity(Long id,
                                     Integer tableNumber,
                                     Integer capacity,
                                     String location,
                                     TableStatus status,
                                     Long restaurantId,
                                     Double x,
                                     Double y,
                                     Double width,
                                     Double height,
                                     Double rotation,
                                     String shapeType,
                                     String label,
                                     String color) {
        RestaurantTable table = new RestaurantTable();
        table.setId(id);
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setLocation(location);
        table.setStatus(status);
        table.setX(x);
        table.setY(y);
        table.setWidth(width);
        table.setHeight(height);
        table.setRotation(rotation);
        table.setShapeType(shapeType);
        table.setLabel(label);
        table.setColor(color);
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
            Long restaurantId,
            Double x,
            Double y,
            Double width,
            Double height,
            Double rotation,
            String shapeType,
            String label,
            String color
    ) {
    }

    private record RestaurantTableUpdateRequest(
            Long id,
            Integer tableNumber,
            Integer capacity,
            String location,
            TableStatus status,
            Long restaurantId,
            Double x,
            Double y,
            Double width,
            Double height,
            Double rotation,
            String shapeType,
            String label,
            String color
    ) {
    }
}
