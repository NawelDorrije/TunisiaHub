package org.example.backend_tunisiahub.Services.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Vehicle;

import java.util.List;

public interface IVehicleService {

    List<Vehicle> retrieveAllVehicles(Long ownerId);

    Vehicle retrieveVehicle(Long id, Long ownerId);

    Vehicle addVehicle(Vehicle request, Long ownerId);

    Vehicle modifyVehicle(Long id, Long ownerId, Vehicle request);

    void removeVehicle(Long id, Long ownerId);
}
