package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {
    List<Reservation> findByRestaurant_IdOrderByDateTimeAsc(Long restaurantId);

    List<Reservation> findByRestaurant_IdAndStatusOrderByDateTimeAsc(Long restaurantId, ReservationStatus status);

    List<Reservation> findByTypeOrderByIdDesc(ReservationType type);
}
