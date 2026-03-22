package org.example.backend_tunisiahub.carpooling.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend_tunisiahub.carpooling.entity.Vehicle;
import org.example.backend_tunisiahub.carpooling.service.IVehicleService;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.example.backend_tunisiahub.shared.security.CurrentUserResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver/vehicles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Driver Vehicles")
public class VehicleController {

    private final IVehicleService vehicleService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @Operation(summary = "Create vehicle")
    public ResponseEntity<VehicleView> createVehicle(@Valid @RequestBody VehicleWriteRequest request,
                                                     HttpServletRequest httpRequest) {
        ensureUserRole(httpRequest);
        Long currentUserId = currentUserResolver.getUserId(httpRequest);
        log.info("Create vehicle request received for userId={}, model={}, plateNumber={}, color={}",
            currentUserId,
            request.model(),
            request.plateNumber(),
            request.color());
        Vehicle response = vehicleService.createVehicle(toVehicle(request), currentUserId);
        log.info("Create vehicle succeeded for userId={}, vehicleId={}", currentUserId, response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toView(response));
    }

    @GetMapping
    @Operation(summary = "List my vehicles")
    public Page<VehicleView> getMyVehicles(HttpServletRequest request,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        ensureUserRole(request);
        Long currentUserId = currentUserResolver.getUserId(request);
        return vehicleService.getMyVehicles(currentUserId, PageRequest.of(page, size)).map(this::toView);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get my vehicle details")
    public VehicleView getVehicle(@PathVariable Long id, HttpServletRequest request) {
        ensureUserRole(request);
        Long currentUserId = currentUserResolver.getUserId(request);
        return toView(vehicleService.getVehicle(id, currentUserId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle")
    public VehicleView updateVehicle(@PathVariable Long id,
                                     @Valid @RequestBody VehicleWriteRequest payload,
                                     HttpServletRequest request) {
        ensureUserRole(request);
        Long currentUserId = currentUserResolver.getUserId(request);
        return toView(vehicleService.updateVehicle(id, currentUserId, toVehicle(payload)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id, HttpServletRequest request) {
        ensureUserRole(request);
        Long currentUserId = currentUserResolver.getUserId(request);
        vehicleService.deleteVehicle(id, currentUserId);
    }

    private void ensureUserRole(HttpServletRequest request) {
        String role = currentUserResolver.getRole(request);
        if (!"USER".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with USER role can perform this action");
        }
    }

    private Vehicle toVehicle(VehicleWriteRequest payload) {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(payload.model());
        vehicle.setPlateNumber(payload.plateNumber());
        vehicle.setColor(payload.color());
        return vehicle;
    }

    private VehicleView toView(Vehicle vehicle) {
        return new VehicleView(
                vehicle.getId(),
                vehicle.getModel(),
                vehicle.getPlateNumber(),
                vehicle.getColor()
        );
    }

    private record VehicleWriteRequest(
            @NotBlank String model,
            @NotBlank String plateNumber,
            @NotBlank String color
    ) {
    }

    private record VehicleView(
            Long id,
            String model,
            String plateNumber,
            String color
    ) {
    }
}
