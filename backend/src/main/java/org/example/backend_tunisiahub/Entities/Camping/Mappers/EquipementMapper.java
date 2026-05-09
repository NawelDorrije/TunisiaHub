package org.example.backend_tunisiahub.Entities.Camping.Mappers;


import org.example.backend_tunisiahub.Entities.Camping.DTO.EquipementDTO;
import org.example.backend_tunisiahub.Entities.Camping.Equipement;
import org.springframework.stereotype.Component;

@Component
public class EquipementMapper {

    public EquipementDTO toDTO(Equipement equipement) {
        if (equipement == null) return null;

        return EquipementDTO.builder()
                .id(equipement.getId())
                .name(equipement.getName())
                .description(equipement.getDescription())
                .quantity(equipement.getQuantity())
                .available(equipement.getAvailable())
                .condition(equipement.getCondition())
                .spotId(equipement.getSpot().getId())
                .spotName(equipement.getSpot().getName())
                .build();
    }

    public void updateEntityFromDTO(EquipementDTO dto, Equipement equipement) {
        equipement.setName(dto.getName());
        equipement.setDescription(dto.getDescription());
        equipement.setQuantity(dto.getQuantity());
        equipement.setAvailable(dto.getAvailable() != null ? dto.getAvailable() : true);
        equipement.setCondition(dto.getCondition());
    }
}