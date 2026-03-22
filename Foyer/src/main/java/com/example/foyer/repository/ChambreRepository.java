package com.example.foyer.repository;

import com.example.foyer.entities.Chambre;
import com.example.foyer.entities.TypeChambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
	List<Chambre> findByTypeCAndNumeroChambre(TypeChambre typeC, Long numeroChambre);
}
