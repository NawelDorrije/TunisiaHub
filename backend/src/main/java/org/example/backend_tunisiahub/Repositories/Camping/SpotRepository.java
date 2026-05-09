package org.example.backend_tunisiahub.Repositories.Camping;

<<<<<<< HEAD
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotRepository extends JpaRepository<Spot,Long> {
=======
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotStatus;
import org.example.backend_tunisiahub.Entities.Camping.Enums.SpotType;
import org.example.backend_tunisiahub.Entities.Camping.Enums.ViewType;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

  // ── BASE ───────────────────────────────────────────────

  List<Spot> findByCampingId(Long campingId);

  // ── OWNER filters ──────────────────────────────────────

  List<Spot> findByCampingIdAndStatus(Long campingId, SpotStatus status);

  List<Spot> findByCampingIdAndActive(Long campingId, Boolean active);

  List<Spot> findByCampingIdAndType(Long campingId, SpotType type);

  // Count spots by status for a camping (dashboard stat)
  long countByCampingIdAndStatus(Long campingId, SpotStatus status);

  // ── CLIENT filters ─────────────────────────────────────

  // Filter active & available spots
  List<Spot> findByCampingIdAndStatusAndActive(Long campingId, SpotStatus status, Boolean active);

  // Filter by type + camping
  List<Spot> findByCampingIdAndTypeAndStatus(Long campingId, SpotType type, SpotStatus status);

  // Filter by view type
  List<Spot> findByCampingIdAndViewType(Long campingId, ViewType viewType);

  // Filter by accessibility
  List<Spot> findByCampingIdAndAccessibleForDisabled(Long campingId, Boolean accessibleForDisabled);

  // Filter by shade
  List<Spot> findByCampingIdAndHasShade(Long campingId, Boolean hasShade);

  // Filter by price range
  List<Spot> findByCampingIdAndBasePriceBetween(Long campingId, BigDecimal min, BigDecimal max);

  // Filter by max price
  List<Spot> findByCampingIdAndBasePriceLessThanEqual(Long campingId, BigDecimal maxPrice);

  // Filter by minimum capacity
  List<Spot> findByCampingIdAndCapacityGreaterThanEqual(Long campingId, Integer minCapacity);

  // ── ADVANCED: available spots for date range ───────────

  @Query("""
        SELECT s FROM Spot s
        WHERE s.camping.id = :campingId
        AND s.active = true
        AND s.status = 'LIBRE'
        AND NOT EXISTS (
            SELECT r FROM Reservation r
            WHERE r.spot = s
            AND r.status NOT IN (
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CANCELLED,
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.COMPLETED
                )
            AND r.checkIn < :checkOut
            AND r.checkOut > :checkIn
        )
    """)
  List<Spot> findAvailableSpotsByDates(
    @Param("campingId") Long campingId,
    @Param("checkIn") LocalDate checkIn,
    @Param("checkOut") LocalDate checkOut
  );

  // ── FULL smart filter for CLIENT search ────────────────

  @Query("""
        SELECT s FROM Spot s
        WHERE s.camping.id = :campingId
        AND s.active = true
        AND s.status = 'LIBRE'
        AND (:type IS NULL OR s.type = :type)
        AND (:viewType IS NULL OR s.viewType = :viewType)
        AND (:hasShade IS NULL OR s.hasShade = :hasShade)
        AND (:accessibleForDisabled IS NULL OR s.accessibleForDisabled = :accessibleForDisabled)
        AND (:minCapacity IS NULL OR s.capacity >= :minCapacity)
        AND (:maxPrice IS NULL OR s.basePrice <= :maxPrice)
        AND NOT EXISTS (
            SELECT r FROM Reservation r
            WHERE r.spot = s
            AND r.status NOT IN (
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.CANCELLED,
                    org.example.backend_tunisiahub.Entities.Camping.Enums.ReservationStatus.COMPLETED
                )
            AND r.checkIn < :checkOut
            AND r.checkOut > :checkIn
        )
    """)
  List<Spot> findAvailableSpotsWithFilters(
    @Param("campingId") Long campingId,
    @Param("checkIn") LocalDate checkIn,
    @Param("checkOut") LocalDate checkOut,
    @Param("type") SpotType type,
    @Param("viewType") ViewType viewType,
    @Param("hasShade") Boolean hasShade,
    @Param("accessibleForDisabled") Boolean accessibleForDisabled,
    @Param("minCapacity") Integer minCapacity,
    @Param("maxPrice") BigDecimal maxPrice
  );

  // ── ADMIN ──────────────────────────────────────────────

  List<Spot> findByStatus(SpotStatus status);
  List<Spot> findByType(SpotType type);
  long countByCampingId(Long campingId);
>>>>>>> origin/feature/integrated-app-event
}
