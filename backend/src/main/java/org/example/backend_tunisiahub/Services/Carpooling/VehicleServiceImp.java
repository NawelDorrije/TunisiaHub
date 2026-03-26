package org.example.backend_tunisiahub.Services.Carpooling;

import lombok.AllArgsConstructor;
import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.example.backend_tunisiahub.Repositories.Carpooling.VehicleRepository;
import org.example.backend_tunisiahub.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class VehicleServiceImp implements IVehicleService {

    private VehicleRepository vehicleRepository;

    @Override
    public List<Vehicle> retrieveAllVehicles(Long ownerId) {
        return vehicleRepository.findByOwnerIdOrderByIdDesc(String.valueOf(ownerId));
    }

    @Override
    public Vehicle retrieveVehicle(Long id, Long ownerId) {
        Vehicle vehicle = vehicleRepository.findByIdAndOwnerId(id, String.valueOf(ownerId));
        if (vehicle == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found");
        }
        return vehicle;
    }

    @Override
    public Vehicle addVehicle(Vehicle request, Long ownerId) {
        validateVehicle(request);
        String plateNumber = request.getPlateNumber().trim().toUpperCase();
        if (vehicleRepository.existsByPlateNumberIgnoreCase(plateNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "plateNumber already exists");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setModel(request.getModel().trim());
        vehicle.setPlateNumber(plateNumber);
        vehicle.setColor(request.getColor().trim());
        vehicle.setOwnerId(String.valueOf(ownerId));

        return vehicleRepository.save(vehicle);
    }

    @Override
    public Vehicle modifyVehicle(Long id, Long ownerId, Vehicle request) {
        validateVehicle(request);
        Vehicle vehicle = retrieveVehicle(id, ownerId);
        String plateNumber = request.getPlateNumber().trim().toUpperCase();

        if (vehicleRepository.existsByPlateNumberIgnoreCaseAndIdNot(plateNumber, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "plateNumber already exists");
        }

        vehicle.setModel(request.getModel().trim());
        vehicle.setPlateNumber(plateNumber);
        vehicle.setColor(request.getColor().trim());

        return vehicleRepository.save(vehicle);
    }

    @Override
    public void removeVehicle(Long id, Long ownerId) {
        Vehicle vehicle = retrieveVehicle(id, ownerId);
        vehicleRepository.delete(vehicle);
    }

    private void validateVehicle(Vehicle request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vehicle is required");
        }
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
