package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.ReservationRestaurant;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantStatus;
import org.example.backend_tunisiahub.Entities.ReservationRestaurantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRestaurantRepository  extends JpaRepository<ReservationRestaurant,Long> {
    List<ReservationRestaurant> findByUser_IdOrderByDateTimeDesc(Long userId);

    Optional<ReservationRestaurant> findByCheckInToken(String checkInToken);

    @Query("""
            select distinct r
            from ReservationRestaurant r
            left join fetch r.user
            left join fetch r.restaurant
            left join fetch r.tables
            where r.id = :reservationId
            """)
    Optional<ReservationRestaurant> findDetailedById(@Param("reservationId") Long reservationId);

    List<ReservationRestaurant> findByRestaurant_IdOrderByDateTimeAsc(Long restaurantId);

    List<ReservationRestaurant> findByRestaurant_IdAndStatusOrderByDateTimeAsc(Long restaurantId, ReservationRestaurantStatus status);

    @Query("""
            select r
            from ReservationRestaurant r
            where r.restaurant.id = :restaurantId
              and r.type = :type
              and r.dateTime >= :startOfDay
              and r.dateTime < :endOfDay
              and r.status <> :excludedStatus
            order by r.dateTime asc
            """)
    List<ReservationRestaurant> findForRestaurantSuggestionDay(
            @Param("restaurantId") Long restaurantId,
            @Param("type") ReservationRestaurantType type,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay,
            @Param("excludedStatus") ReservationRestaurantStatus excludedStatus
    );

    List<ReservationRestaurant> findByTypeOrderByIdDesc(ReservationRestaurantType type);

    @Query("""
            select r
            from ReservationRestaurant r
            left join fetch r.restaurant restaurant
            where r.user.id = :userId
              and r.type = :type
              and r.restaurant is not null
              and r.status <> :excludedStatus
              and (r.dateTime is null or r.dateTime <= :referenceDateTime)
            order by r.dateTime desc, r.id desc
            """)
    List<ReservationRestaurant> findRecommendationHistoryByUser(
            @Param("userId") Long userId,
            @Param("type") ReservationRestaurantType type,
            @Param("excludedStatus") ReservationRestaurantStatus excludedStatus,
            @Param("referenceDateTime") LocalDateTime referenceDateTime
    );

    @Query("""
            select r
            from ReservationRestaurant r
            where r.user.id = :userId
              and r.type = :type
              and r.restaurant is not null
              and r.status <> :excludedStatus
              and r.dateTime >= :cutoffDateTime
            order by r.dateTime desc, r.id desc
            """)
    List<ReservationRestaurant> findRecentRestaurantReservationsByUser(
            @Param("userId") Long userId,
            @Param("type") ReservationRestaurantType type,
            @Param("excludedStatus") ReservationRestaurantStatus excludedStatus,
            @Param("cutoffDateTime") LocalDateTime cutoffDateTime
    );

    List<ReservationRestaurant> findByRestaurant_IdAndTypeAndDateTimeAndStatusIn(
            Long restaurantId,
            ReservationRestaurantType type,
            LocalDateTime dateTime,
            Collection<ReservationRestaurantStatus> statuses
    );

    @Query("""
            select distinct r
            from ReservationRestaurant r
            join r.tables t
            where r.restaurant.id = :restaurantId
              and r.type = :type
              and r.dateTime = :dateTime
              and r.status in :statuses
              and (:excludedReservationId is null or r.id <> :excludedReservationId)
              and t.id in :tableIds
            """)
    List<ReservationRestaurant> findTableConflicts(
            @Param("restaurantId") Long restaurantId,
            @Param("type") ReservationRestaurantType type,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("statuses") Collection<ReservationRestaurantStatus> statuses,
            @Param("tableIds") Collection<Long> tableIds,
            @Param("excludedReservationId") Long excludedReservationId
    );
}
