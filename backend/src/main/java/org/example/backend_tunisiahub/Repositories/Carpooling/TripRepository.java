package org.example.backend_tunisiahub.Repositories.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByStatusIgnoreCaseOrderByDepartureDateTimeAsc(String status);

    List<Trip> findByCreatedByOrDriverIdOrderByDepartureDateTimeDesc(String createdBy, String driverId);

    Trip findByIdAndCreatedBy(Long id, String createdBy);
}
