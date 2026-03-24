package org.example.backend_tunisiahub.carpooling.repository;

import org.example.backend_tunisiahub.carpooling.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Page<Vehicle> findByOwnerIdOrderByIdDesc(String ownerId, Pageable pageable);

    Optional<Vehicle> findByIdAndOwnerId(Long id, String ownerId);

    boolean existsByPlateNumberIgnoreCase(String plateNumber);

    boolean existsByPlateNumberIgnoreCaseAndIdNot(String plateNumber, Long id);
}
