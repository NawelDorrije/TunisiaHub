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
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findBySpotId(Long spotId);

    List<Reservation> findByStatus(ReservationStatus status);

    /**
     * Returns true when there is already an active reservation for the given
     * spot whose dates overlap [checkIn, checkOut).
     *
     * <p>Overlap condition: existing.checkIn < newCheckOut
     *                   AND existing.checkOut > newCheckIn
     *
     * <p>Reservations in terminal states (CANCELLED, COMPLETED) are excluded —
     * they must never block a new booking.
     */
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.spot.id = :spotId
              AND r.status NOT IN (
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CANCELLED,
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.COMPLETED
                  )
              AND r.checkIn  < :checkOut
              AND r.checkOut > :checkIn
            """)
    boolean existsOverlappingReservation(
            @Param("spotId")   Long      spotId,
            @Param("checkIn")  LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /*

    // ── Pricing signal queries ─────────────────────────────────────────────────

    /**
     * Count active reservations in a camping on a specific date.
     * Used to compute the real-time occupancy rate signal.
     */

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.spot.camping.id = :campingId
        AND r.status NOT IN ('CANCELLED', 'COMPLETED')
        AND r.checkIn <= :date
        AND r.checkOut > :date
    """)
    long countActiveReservationsForCampingOnDate(
            @Param("campingId") Long campingId,
            @Param("date") LocalDate date
    );

    /**
     * Count bookings CREATED between two dates (not check-in dates).
     * Used to compute the demand index vs. same window last year.
     */

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.spot.camping.id = :campingId
        AND CAST(r.createdAt AS date) >= :from
        AND CAST(r.createdAt AS date) <= :to
    """)
    long countBookingsMadeBetween(
            @Param("campingId") Long campingId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );



}