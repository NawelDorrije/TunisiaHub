package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IVehicleService {

    Vehicle createVehicle(Vehicle request, Long ownerId);

    Page<Vehicle> getMyVehicles(Long ownerId, Pageable pageable);

    Vehicle getVehicle(Long id, Long ownerId);

    Vehicle updateVehicle(Long id, Long ownerId, Vehicle request);

    void deleteVehicle(Long id, Long ownerId);
}
