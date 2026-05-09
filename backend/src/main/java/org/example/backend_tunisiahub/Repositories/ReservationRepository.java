package org.example.backend_tunisiahub.Repositories;

<<<<<<< HEAD
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
=======
import org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus;
import org.example.backend_tunisiahub.Entities.Reservation;
>>>>>>> origin/feature/integrated-app-event
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {
    List<Reservation> findByUser_IdOrderByDateTimeDesc(Long userId);

    Optional<Reservation> findByCheckInToken(String checkInToken);

    @Query("""
            select distinct r
            from Reservation r
            left join fetch r.user
            left join fetch r.restaurant
            left join fetch r.tables
            where r.id = :reservationId
            """)
    Optional<Reservation> findDetailedById(@Param("reservationId") Long reservationId);

    List<Reservation> findByRestaurant_IdOrderByDateTimeAsc(Long restaurantId);

    List<Reservation> findByRestaurant_IdAndStatusOrderByDateTimeAsc(Long restaurantId, ReservationStatus status);

    @Query("""
            select r
            from Reservation r
            where r.restaurant.id = :restaurantId
              and r.type = :type
              and r.dateTime >= :startOfDay
              and r.dateTime < :endOfDay
              and r.status <> :excludedStatus
            order by r.dateTime asc
            """)
    List<Reservation> findForRestaurantSuggestionDay(
            @Param("restaurantId") Long restaurantId,
            @Param("type") ReservationType type,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("excludedStatus") ReservationStatus excludedStatus
    );

    List<Reservation> findByTypeOrderByIdDesc(ReservationType type);

    @Query("""
            select r
            from Reservation r
            left join fetch r.restaurant restaurant
            where r.user.id = :userId
              and r.type = :type
              and r.restaurant is not null
              and r.status <> :excludedStatus
              and (r.dateTime is null or r.dateTime <= :referenceDateTime)
            order by r.dateTime desc, r.id desc
            """)
    List<Reservation> findRecommendationHistoryByUser(
            @Param("userId") Long userId,
            @Param("type") ReservationType type,
            @Param("excludedStatus") ReservationStatus excludedStatus,
            @Param("referenceDateTime") LocalDateTime referenceDateTime
    );

    @Query("""
            select r
            from Reservation r
            where r.user.id = :userId
              and r.type = :type
              and r.restaurant is not null
              and r.status <> :excludedStatus
              and r.dateTime >= :cutoffDateTime
            order by r.dateTime desc, r.id desc
            """)
    List<Reservation> findRecentRestaurantReservationsByUser(
            @Param("userId") Long userId,
            @Param("type") ReservationType type,
            @Param("excludedStatus") ReservationStatus excludedStatus,
            @Param("cutoffDateTime") LocalDateTime cutoffDateTime
    );

    List<Reservation> findByRestaurant_IdAndTypeAndDateTimeAndStatusIn(
            Long restaurantId,
            ReservationType type,
            LocalDateTime dateTime,
            Collection<ReservationStatus> statuses
    );

    @Query("""
            select distinct r
            from Reservation r
            join r.tables t
            where r.restaurant.id = :restaurantId
              and r.type = :type
              and r.dateTime = :dateTime
              and r.status in :statuses
              and (:excludedReservationId is null or r.id <> :excludedReservationId)
              and t.id in :tableIds
            """)
    List<Reservation> findTableConflicts(
            @Param("restaurantId") Long restaurantId,
            @Param("type") ReservationType type,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("tableIds") Collection<Long> tableIds,
            @Param("excludedReservationId") Long excludedReservationId
=======
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
List<Reservation> findByTripId(Long tripId);
    List<Reservation> findByTripIdOrderByIdDesc(Long tripId);
    long countByTripId(Long tripId);
    List<Reservation> findByReservedBy_Id(Long userId);
    Reservation findByIdAndTrip_Driver_Id(Long reservationId, Long driverId);

  @Query("select r from Reservation r where r.trip is not null and " +
    "(:tripId is null or r.trip.id = :tripId) and " +
    "(:status is null or r.status = :status) " +
    "order by r.id desc")
  List<Reservation> findAdminTripReservations(@Param("tripId") Long tripId,
                                              @Param("status") ReservationStatus status);

    @Query("select count(r) from Reservation r where r.trip.driver.id = :driverId")
    long countByTripDriverId(@Param("driverId") Long driverId);

    @Query("select count(r) from Reservation r where r.trip.driver.id = :driverId and " +
            "lower(coalesce(r.status, '')) in ('canceled', 'cancelled')")
    long countCanceledByTripDriverId(@Param("driverId") Long driverId);
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
>>>>>>> origin/feature/integrated-app-event
    );
}
