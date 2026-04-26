package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Entities.ReservationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository  extends JpaRepository<Reservation,Long> {

	@Query("""
		SELECT r
		FROM Reservation r
		WHERE r.type = :type
		  AND r.status = 'CONFIRMED'
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
}
