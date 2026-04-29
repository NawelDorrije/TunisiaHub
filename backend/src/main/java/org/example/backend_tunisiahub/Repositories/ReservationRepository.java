package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {
    List<Reservation> findByTripId(Long tripId);
    List<Reservation> findByTripIdOrderByIdDesc(Long tripId);
    long countByTripId(Long tripId);
    List<Reservation> findByReservedBy_Id(Long userId);
    Reservation findByIdAndTrip_Driver_Id(Long reservationId, Long driverId);

    @Query("select r from Reservation r where r.trip is not null and " +
            "(:tripId is null or r.trip.id = :tripId) and " +
            "(:status is null or lower(r.status) = lower(:status)) " +
            "order by r.id desc")
    List<Reservation> findAdminTripReservations(@Param("tripId") Long tripId,
                                                @Param("status") String status);

    @Query("select count(r) from Reservation r where r.trip.driver.id = :driverId")
    long countByTripDriverId(@Param("driverId") Long driverId);

    @Query("select count(r) from Reservation r where r.trip.driver.id = :driverId and " +
            "lower(coalesce(r.status, '')) in ('canceled', 'cancelled')")
    long countCanceledByTripDriverId(@Param("driverId") Long driverId);
}
