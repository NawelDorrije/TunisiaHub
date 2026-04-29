package org.example.backend_tunisiahub.carpooling.repository;

import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    List<Trip> findByDriverIdOrderByDepartureDateTimeDesc(Long driverId);
}
