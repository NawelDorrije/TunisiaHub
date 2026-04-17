package org.example.backend_tunisiahub.Repositories.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    List<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy);

    Page<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy, Pageable pageable);

    Optional<Trip> findByIdAndCreatedBy(Long id, String createdBy);
}
