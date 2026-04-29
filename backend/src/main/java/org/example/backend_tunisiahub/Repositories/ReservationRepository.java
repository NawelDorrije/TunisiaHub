package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ================= BASIC LOOKUPS =================

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findBySpotId(Long spotId);

    List<Reservation> findByStatus(ReservationStatus status);

    // ================= CAMPING OVERLAP =================

    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.spot.id = :spotId
          AND r.status NOT IN (
                org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CANCELLED,
                org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.COMPLETED
          )
          AND r.checkIn < :checkOut
          AND r.checkOut > :checkIn
    """)
    boolean existsOverlappingReservation(
            @Param("spotId") Long spotId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    // ================= REMINDER (UPDATED FROM OLD ACCOMMODATION LOGIC) =================
    // OLD: type + startDate + reminderSentAt + accommodation
    // NEW: we reuse checkIn + status + accommodation (still exists in entity)

    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.type = :type
          AND r.status = org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CONFIRMED
          AND r.startDate = :targetStartDate
          AND r.reminderSentAt IS NULL
          AND r.user IS NOT NULL
          AND r.user.email IS NOT NULL
          AND r.user.email <> ''
          AND r.accommodation IS NOT NULL
    """)
    List<Reservation> findPendingAccommodationReminderReservations(
            @Param("type") ReservationType type,
            @Param("targetStartDate") Date targetStartDate
    );

    // ================= CAMPING ANALYTICS =================

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.spot.camping.id = :campingId
          AND r.status NOT IN (
                org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CANCELLED,
                org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.COMPLETED
          )
          AND r.checkIn <= :date
          AND r.checkOut > :date
    """)
    long countActiveReservationsForCampingOnDate(
            @Param("campingId") Long campingId,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.spot.camping.id = :campingId
          AND CAST(r.createdAt AS date) BETWEEN :from AND :to
    """)
    long countBookingsMadeBetween(
            @Param("campingId") Long campingId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}