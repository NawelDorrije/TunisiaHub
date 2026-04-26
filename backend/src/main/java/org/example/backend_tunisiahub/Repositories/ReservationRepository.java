package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationStatus;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    List<Reservation> findByTypeOrderByIdDesc(ReservationType type);

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
    );
}
