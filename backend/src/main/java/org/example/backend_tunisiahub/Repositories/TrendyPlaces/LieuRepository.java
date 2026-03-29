package org.example.backend_tunisiahub.Repositories.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.Lieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LieuRepository extends JpaRepository<Lieu, Long> {
}