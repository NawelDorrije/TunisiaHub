package org.example.backend_tunisiahub.Repositories.Accommodation;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface AccommodationReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByAccommodationId(Long accommodationId);
    List<Reservation> findByUserAndType(User user, ReservationType type);
    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.accommodation.id = :accommodationId " +
            "AND r.status = 'CONFIRMED' " +
            "AND r.startDate <= :endDate " +
            "AND r.endDate >= :startDate")
    boolean existsOverlappingReservation(
            @Param("accommodationId") Long accommodationId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}