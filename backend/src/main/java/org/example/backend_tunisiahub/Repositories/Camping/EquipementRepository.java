package org.example.backend_tunisiahub.Repositories.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;
import org.example.backend_tunisiahub.Entities.Camping.Equipement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipementRepository extends JpaRepository<Equipement, Long> {

    // Filter by spot
    List<Equipement> findBySpotId(Long spotId);

    // Filter by availability
    List<Equipement> findByAvailable(Boolean available);

    // Filter by condition
    List<Equipement> findByCondition(EquipmentCondition condition);

    // Filter by spot + availability
    List<Equipement> findBySpotIdAndAvailable(Long spotId, Boolean available);

    // Filter by spot + condition
    List<Equipement> findBySpotIdAndCondition(Long spotId, EquipmentCondition condition);
}