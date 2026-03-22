package com.example.foyer.repository;

import com.example.foyer.entities.Foyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoyerRepository extends JpaRepository<Foyer, Long> {
	Foyer findByNomFoyer(String nomFoyer);
	Foyer findByNomFoyerAndCapaciteFoyerLessThan(String nomFoyer, long capaciteFoyer);
}
