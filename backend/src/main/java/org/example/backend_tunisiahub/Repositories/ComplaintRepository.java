package org.example.backend_tunisiahub.Repositories;

import org.example.backend_tunisiahub.Entities.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    @Query("select count(c) from Complaint c where c.reservation.trip.driver.id = :driverId")
    long countByDriverId(@Param("driverId") Long driverId);
}
