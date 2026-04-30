package org.example.backend_tunisiahub.carpooling.repository;

import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    List<Trip> findByDriverIdOrderByDepartureDateTimeDesc(Long driverId);
    Page<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy, Pageable pageable);
    List<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy);
    Optional<Trip> findByIdAndCreatedBy(Long id, String createdBy);
}
