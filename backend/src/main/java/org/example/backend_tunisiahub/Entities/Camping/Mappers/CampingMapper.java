package org.example.backend_tunisiahub.Entities.Camping.Mappers;

import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.DTO.CampingDTO;
import org.springframework.stereotype.Component;

@Component
public class CampingMapper {

    /**
     * Entity → DTO (for responses)
     */
    public CampingDTO toDTO(Camping camping) {
        if (camping == null) return null;

        return CampingDTO.builder()
                .id(camping.getId())
                .createdAt(camping.getCreatedAt())
                .updatedAt(camping.getUpdatedAt())
                .ownerId(camping.getOwner().getId())
                .ownerName(camping.getOwner().getNom())
                .name(camping.getName())
                .address(camping.getAddress())
                .governorate(camping.getGovernorate())
                .latitude(camping.getLatitude())
                .longitude(camping.getLongitude())
                .averageRating(camping.getAverageRating())
                .numberOfSpots(camping.getNumberOfSpots())
                .maxCapacity(camping.getMaxCapacity())
                .status(camping.getStatus())
                .rules(camping.getRules())
                .checkInTime(camping.getCheckInTime())
                .checkOutTime(camping.getCheckOutTime())
                .price(camping.getPrice())
                .description(camping.getDescription())
                .startDate(camping.getStartDate())
                .endDate(camping.getEndDate())
                .photos(camping.getPhotos())
                .build();
    }

    /**
     * DTO → Entity (for create/update)
     */
    public Camping toEntity(CampingDTO dto) {
        if (dto == null) return null;

        Camping camping = new Camping();
        camping.setName(dto.getName());
        camping.setAddress(dto.getAddress());
        camping.setGovernorate(dto.getGovernorate());
        camping.setLatitude(dto.getLatitude());
        camping.setLongitude(dto.getLongitude());
        camping.setMaxCapacity(dto.getMaxCapacity());
        camping.setStatus(dto.getStatus());
        camping.setRules(dto.getRules());
        camping.setCheckInTime(dto.getCheckInTime());
        camping.setCheckOutTime(dto.getCheckOutTime());
        camping.setPrice(dto.getPrice());
        camping.setDescription(dto.getDescription());
        camping.setStartDate(dto.getStartDate());
        camping.setEndDate(dto.getEndDate());
        camping.setPhotos(dto.getPhotos());
        return camping;
    }

    /**
     * Apply DTO fields onto an existing entity (update)
     */
    public void updateEntityFromDTO(CampingDTO dto, Camping camping) {
        camping.setName(dto.getName());
        camping.setAddress(dto.getAddress());
        camping.setGovernorate(dto.getGovernorate());
        camping.setLatitude(dto.getLatitude());
        camping.setLongitude(dto.getLongitude());
        camping.setMaxCapacity(dto.getMaxCapacity());
        camping.setStatus(dto.getStatus());
        camping.setRules(dto.getRules());
        camping.setCheckInTime(dto.getCheckInTime());
        camping.setCheckOutTime(dto.getCheckOutTime());
        camping.setPrice(dto.getPrice());
        camping.setDescription(dto.getDescription());
        camping.setStartDate(dto.getStartDate());
        camping.setEndDate(dto.getEndDate());
        if (dto.getPhotos() != null) {
            camping.setPhotos(dto.getPhotos());
        }
    }
}
