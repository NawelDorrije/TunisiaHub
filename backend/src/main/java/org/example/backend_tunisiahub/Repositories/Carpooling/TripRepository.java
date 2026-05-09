<<<<<<< HEAD
package org.example.backend_tunisiahub.carpooling.repository;

import org.example.backend_tunisiahub.carpooling.entity.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip> {
    List<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy);

    Page<Trip> findByCreatedByOrderByDepartureDateTimeDesc(String createdBy, Pageable pageable);

    Optional<Trip> findByIdAndCreatedBy(Long id, String createdBy);
=======
package org.example.backend_tunisiahub.Repositories.Carpooling;

import org.example.backend_tunisiahub.Entities.Carpooling.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

  @Query("select t from Trip t where " +
    "(:status is null or lower(t.status) = lower(:status)) and " +
    "(:departurePoint is null or lower(trim(case when locate(',', t.departure) > 0 " +
    "then substring(t.departure, 1, locate(',', t.departure) - 1) else t.departure end)) " +
    "like lower(concat('%', :departurePoint, '%'))) and " +
    "(:destination is null or lower(trim(case when locate(',', t.destination) > 0 " +
    "then substring(t.destination, 1, locate(',', t.destination) - 1) else t.destination end)) " +
    "like lower(concat('%', :destination, '%'))) and " +
    "(:dateFrom is null or t.departureDateTime >= :dateFrom) and " +
    "(:dateTo is null or t.departureDateTime < :dateTo) and " +
    "(:seatsRequired is null or " +
    "(t.seatsTotal - coalesce((select sum(coalesce(r.numberOfPeople, 1)) " +
    "from Reservation r where r.trip = t and lower(coalesce(r.status, '')) " +
    "not in ('canceled', 'cancelled')), 0)) >= :seatsRequired) and " +
    "(:bookingMode is null or lower(t.bookingMode) = lower(:bookingMode)) and " +
    "(:minPrice is null or t.price >= :minPrice) and " +
    "(:maxPrice is null or t.price <= :maxPrice) and " +
    "(:durationMax is null or t.durationMinutes <= :durationMax) " +
    "order by t.departureDateTime asc")
  List<Trip> searchTripsAdvanced(@Param("status") String status,
                                 @Param("departurePoint") String departurePoint,
                                 @Param("destination") String destination,
                                 @Param("dateFrom") LocalDateTime dateFrom,
                                 @Param("dateTo") LocalDateTime dateTo,
                                 @Param("seatsRequired") Integer seatsRequired,
                                 @Param("bookingMode") String bookingMode,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice,
                                 @Param("durationMax") Integer durationMax);

  @Query("select t from Trip t where t.driver.id = :driverId order by t.departureDateTime desc")
  List<Trip> findByDriverIdOrderByDepartureDateTimeDesc(@Param("driverId") Long driverId);

  @Query("select t from Trip t where " +
    "(:status is null or lower(t.status) = lower(:status)) and " +
    "(:departure is null or lower(t.departure) like lower(concat('%', :departure, '%'))) and " +
    "(:destination is null or lower(t.destination) like lower(concat('%', :destination, '%'))) and " +
    "(:driverId is null or t.driver.id = :driverId) and " +
    "(:dateFrom is null or t.departureDateTime >= :dateFrom) and " +
    "(:dateTo is null or t.departureDateTime < :dateTo) " +
    "order by t.departureDateTime desc")
  List<Trip> findAdminTrips(@Param("status") String status,
                            @Param("departure") String departure,
                            @Param("destination") String destination,
                            @Param("driverId") Long driverId,
                            @Param("dateFrom") LocalDateTime dateFrom,
                            @Param("dateTo") LocalDateTime dateTo);

  @Query("select distinct t.driver from Trip t where t.driver is not null")
  List<org.example.backend_tunisiahub.Entities.User.User> findCarpoolingDrivers();

  @Query("select t from Trip t where t.id = :id and t.driver.id = :driverId")
  Trip findByIdAndDriverId(@Param("id") Long id, @Param("driverId") Long driverId);

  List<Trip> findByDepartureDateTimeBeforeOrderByDepartureDateTimeAsc(LocalDateTime departureDateTime);

  @Query("select " +
    "(t.seatsTotal - coalesce((select sum(coalesce(r.numberOfPeople, 1)) " +
    "from Reservation r where r.trip = t and lower(coalesce(r.status, '')) " +
    "not in ('canceled', 'cancelled')), 0)) " +
    "from Trip t where t.id = :tripId")
  Integer findSeatsAvailableByTripId(@Param("tripId") Long tripId);
>>>>>>> origin/feature/integrated-app-event
}
