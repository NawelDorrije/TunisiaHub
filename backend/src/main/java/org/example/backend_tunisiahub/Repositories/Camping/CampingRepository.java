package org.example.backend_tunisiahub.Repositories.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.Enums.CampingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CampingRepository extends JpaRepository<Camping, Long> {

  // ── OWNER filters ──────────────────────────────────────

  List<Camping> findByOwnerId(Long ownerId);

  List<Camping> findByOwnerIdAndStatus(Long ownerId, CampingStatus status);

  // ── ADMIN filters ──────────────────────────────────────

  List<Camping> findByStatus(CampingStatus status);

  List<Camping> findByGovernorate(String governorate);

  List<Camping> findByGovernorateAndStatus(String governorate, CampingStatus status);



  // ── CLIENT filters ─────────────────────────────────────

  // Search by name keyword (case-insensitive)
  List<Camping> findByNameContainingIgnoreCase(String keyword);

  // Filter by max capacity
  List<Camping> findByMaxCapacityGreaterThanEqual(Integer minCapacity);

  // Filter active campings by governorate and price range
  @Query("""
        SELECT c FROM Camping c
        WHERE c.status = 'ACTIVE'
        AND (:governorate IS NULL OR c.governorate = :governorate)
        AND (:minCapacity IS NULL OR c.maxCapacity >= :minCapacity)
    """)
  List<Camping> findAvailableByFilters(
    @Param("governorate") String governorate,
    @Param("minCapacity") Integer minCapacity
  );

  // Find campings that have available spots for given dates
  @Query("""
        SELECT DISTINCT c FROM Camping c
        JOIN c.spots s
        WHERE c.status = 'ACTIVE'
        AND s.active = true
        AND s.status = 'LIBRE'
        AND (:governorate IS NULL OR c.governorate = :governorate)
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
  List<Camping> findCampingsWithAvailableSpotsForDates(
    @Param("checkIn") LocalDate checkIn,
    @Param("checkOut") LocalDate checkOut,
    @Param("governorate") String governorate
  );

  // Find by rating threshold
  List<Camping> findByAverageRatingGreaterThanEqual(BigDecimal minRating);

  // Combined full-text search
  @Query("""
        SELECT c FROM Camping c
        WHERE c.status = 'ACTIVE'
        AND (
            LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(c.governorate) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
  List<Camping> searchByKeyword(@Param("keyword") String keyword);

  @Query("""
    SELECT c FROM Camping c
    WHERE c.status = 'ACTIVE'
    AND c.startDate IS NOT NULL AND c.endDate IS NOT NULL
    AND c.endDate >= :today
""")
  List<Camping> findAvailableCampingsForDates(@Param("today") LocalDate today);
}
