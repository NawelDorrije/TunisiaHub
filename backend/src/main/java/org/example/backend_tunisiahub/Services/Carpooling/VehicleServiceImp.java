package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Repositories.Carpooling.VehicleRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImp implements IVehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    @Override
    public Vehicle createVehicle(Vehicle request, Long ownerId) {
        if (request == null) {
            log.warn("Create vehicle failed: request payload is null for ownerId={}", ownerId);
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vehicle payload is required");
        }
        log.debug("Create vehicle validation started for ownerId={}, model={}, plateNumber={}, color={}",
                ownerId,
                request.getModel(),
                request.getPlateNumber(),
                request.getColor());
        validateVehiclePayload(request);
        String normalizedPlate = normalizePlate(request.getPlateNumber());
        log.debug("Create vehicle normalized plate for ownerId={}: {}", ownerId, normalizedPlate);
        if (vehicleRepository.existsByPlateNumberIgnoreCase(normalizedPlate)) {
            log.warn("Create vehicle failed: duplicate plateNumber={} for ownerId={}", normalizedPlate, ownerId);
            throw new ApiException(HttpStatus.CONFLICT, "plateNumber already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setModel(request.getModel().trim());
        vehicle.setPlateNumber(normalizedPlate);
        vehicle.setColor(request.getColor().trim());
        vehicle.setOwnerId(String.valueOf(ownerId));

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Create vehicle persisted successfully for ownerId={}, vehicleId={}", ownerId, saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Vehicle> getMyVehicles(Long ownerId, Pageable pageable) {
        return vehicleRepository.findByOwnerIdOrderByIdDesc(String.valueOf(ownerId), pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Vehicle getVehicle(Long id, Long ownerId) {
        return vehicleRepository.findByIdAndOwnerId(id, String.valueOf(ownerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    @Transactional
    @Override
    public Vehicle updateVehicle(Long id, Long ownerId, Vehicle request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vehicle payload is required");
        }
        validateVehiclePayload(request);
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerId(id, String.valueOf(ownerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found"));

        String normalizedPlate = normalizePlate(request.getPlateNumber());
        if (vehicleRepository.existsByPlateNumberIgnoreCaseAndIdNot(normalizedPlate, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "plateNumber already exists");
        }

        vehicle.setModel(request.getModel().trim());
        vehicle.setPlateNumber(normalizedPlate);
        vehicle.setColor(request.getColor().trim());

        return vehicleRepository.save(vehicle);
    }

    @Transactional
    @Override
    public void deleteVehicle(Long id, Long ownerId) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerId(id, String.valueOf(ownerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        vehicleRepository.delete(vehicle);
    }

    private String normalizePlate(String plateNumber) {
        return plateNumber == null ? null : plateNumber.trim().toUpperCase();
    }

    private void validateVehiclePayload(Vehicle request) {
        if (request.getModel() == null || request.getModel().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "model is required");
        }
        if (request.getPlateNumber() == null || request.getPlateNumber().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "plateNumber is required");
        }
        if (request.getColor() == null || request.getColor().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "color is required");
        }
    }
}
