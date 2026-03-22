package com.example.foyer.repository;

import com.example.foyer.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByEstValideTrueAndAnneUniversite(LocalDate anneUniversite);
}
