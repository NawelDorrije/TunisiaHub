package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {

    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

}
