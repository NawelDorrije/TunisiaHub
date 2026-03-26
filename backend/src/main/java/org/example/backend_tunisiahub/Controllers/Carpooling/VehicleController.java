package org.example.backend_tunisiahub.Controllers.Carpooling;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Services.Carpooling.IVehicleService;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/driver/vehicles")
@AllArgsConstructor
@Tag(name = "Driver Vehicles")
public class VehicleController {

    IVehicleService vehicleService;
    CurrentUserResolver currentUserResolver;

    @PostMapping
    @Operation(summary = "Create vehicle")
    public Vehicle addVehicle(@RequestBody Vehicle request, HttpServletRequest httpRequest) {
        Long currentUserId = currentUserResolver.getUserId(httpRequest);
        return vehicleService.addVehicle(request, currentUserId);
    }

    @GetMapping
    @Operation(summary = "List my vehicles")
    public List<Vehicle> retrieveAllVehicles(HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return vehicleService.retrieveAllVehicles(currentUserId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my vehicle details")
    public Vehicle retrieveVehicle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return vehicleService.retrieveVehicle(id, currentUserId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle")
    public Vehicle modifyVehicle(@PathVariable Long id,
                                 @RequestBody Vehicle payload,
                                 HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        return vehicleService.modifyVehicle(id, currentUserId, payload);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    public void deleteVehicle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = currentUserResolver.getUserId(request);
        vehicleService.removeVehicle(id, currentUserId);
    }
}
