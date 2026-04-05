package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {

    boolean existsByUserIdAndSpotId(Long userId, Long spotId);

    List<Reservation> findByUserId(Long userId);
    List<Reservation> findBySpotId(Long spotId);
    List<Reservation> findByStatus(ReservationStatus status);

    // Check for overlapping reservations on same spot
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.spot.id = :spotId
        AND r.status NOT IN ('CANCELLED', 'COMPLETED')
        AND r.checkIn < :checkOut
        AND r.checkOut > :checkIn
    """)
    boolean existsOverlappingReservation(
            @Param("spotId") Long spotId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
