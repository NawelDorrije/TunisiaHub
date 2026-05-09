package org.example.backend_tunisiahub.Repositories.TrendyPlaces;

import org.example.backend_tunisiahub.Entities.TrendyPlaces.ActiviteLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiviteLieuRepository extends JpaRepository<ActiviteLieu, Long> {
    List<ActiviteLieu> findByLieuId(Long lieuId);
}