package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
>>>>>>> origin/feature/integrated-app-event
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
<<<<<<< HEAD
=======

  @Query("select count(c) from Complaint c where c.reservation.trip.driver.id = :driverId")
  long countByDriverId(@Param("driverId") Long driverId);
>>>>>>> origin/feature/integrated-app-event
}
