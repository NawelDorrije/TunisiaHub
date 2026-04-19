package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {
    List<Reservation> findByTripId(Long tripId);
    List<Reservation> findByReservedBy_Id(Long userId);
}
