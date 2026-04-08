package org.example.backend_tunisiahub.Repositories.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.ReservationActivite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationActiviteRepository extends JpaRepository<ReservationActivite, Long> {
    List<ReservationActivite> findByUserId(Long userId);
    List<ReservationActivite> findByActiviteId(Long activiteId);
}