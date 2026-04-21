package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    long countByEventId(Long eventId);
    Reservation findByUserIdAndEventId(Long userId, Long eventId);


}
