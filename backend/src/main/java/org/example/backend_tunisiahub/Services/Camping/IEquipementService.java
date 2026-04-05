package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.DTO.EquipementDTO;
import org.example.backend_tunisiahub.Entities.Camping.Enums.EquipmentCondition;

import java.util.List;

public interface IEquipementService {

    EquipementDTO createEquipement(EquipementDTO dto);

    EquipementDTO updateEquipement(Long id, EquipementDTO dto);

    void deleteEquipement(Long id);

    EquipementDTO getEquipementById(Long id);

    List<EquipementDTO> getAllEquipements();

    // Filters
    List<EquipementDTO> getBySpotId(Long spotId);

    List<EquipementDTO> getByAvailability(Boolean available);

    List<EquipementDTO> getByCondition(EquipmentCondition condition);

    List<EquipementDTO> getBySpotAndAvailability(Long spotId, Boolean available);

    List<EquipementDTO> getBySpotAndCondition(Long spotId, EquipmentCondition condition);
}