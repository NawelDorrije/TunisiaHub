package org.example.backend_tunisiahub.Entities.Camping.Mappers;

import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.springframework.stereotype.Component;

@Component
public class SpotMapper {

    /**
     * Entity → DTO (for responses)
     */
    public SpotDTO toDTO(Spot spot) {
        if (spot == null) return null;

        return SpotDTO.builder()
                .id(spot.getId())
                .createdAt(spot.getCreatedAt())

                // Camping reference
                .campingId(
                        spot.getCamping() != null
                                ? spot.getCamping().getId()
                                : null
                )
                .campingName(
                        spot.getCamping() != null
                                ? spot.getCamping().getName()
                                : null
                )

                // Core fields
                .name(spot.getName())
                .type(spot.getType())
                .capacity(spot.getCapacity())
                .area(spot.getArea())
                .description(spot.getDescription())
                .basePrice(spot.getBasePrice())
                .status(spot.getStatus())
                .positionX(spot.getPositionX())
                .positionY(spot.getPositionY())
                .viewType(spot.getViewType())
                .hasShade(spot.getHasShade())
                .accessibleForDisabled(spot.getAccessibleForDisabled())
                .active(spot.getActive())
                .photos(spot.getPhotos())

                .build();
    }

    /**
     * DTO → Entity (for create — camping is set separately in the service)
     */
    public Spot toEntity(SpotDTO dto) {
        if (dto == null) return null;

        Spot spot = new Spot();

        spot.setName(dto.getName());
        spot.setType(dto.getType());
        spot.setCapacity(dto.getCapacity());
        spot.setArea(dto.getArea());
        spot.setDescription(dto.getDescription());
        spot.setBasePrice(dto.getBasePrice());
        spot.setStatus(dto.getStatus());
        spot.setPositionX(dto.getPositionX());
        spot.setPositionY(dto.getPositionY());
        spot.setViewType(dto.getViewType());

        // Safe boolean defaults
        spot.setHasShade(
                dto.getHasShade() != null
                        ? dto.getHasShade()
                        : false
        );

        spot.setAccessibleForDisabled(
                dto.getAccessibleForDisabled() != null
                        ? dto.getAccessibleForDisabled()
                        : false
        );

        spot.setActive(
                dto.getActive() != null
                        ? dto.getActive()
                        : true
        );

        if (dto.getPhotos() != null) {
            spot.setPhotos(dto.getPhotos());
        }

        return spot;
    }

    /**
     * Update existing entity from DTO (for PUT)
     * Avoids overwriting id and createdAt
     */
    public void updateEntityFromDTO(SpotDTO dto, Spot spot) {

        spot.setName(dto.getName());
        spot.setType(dto.getType());
        spot.setCapacity(dto.getCapacity());
        spot.setArea(dto.getArea());
        spot.setDescription(dto.getDescription());
        spot.setBasePrice(dto.getBasePrice());
        spot.setStatus(dto.getStatus());
        spot.setPositionX(dto.getPositionX());
        spot.setPositionY(dto.getPositionY());
        spot.setViewType(dto.getViewType());

        if (dto.getHasShade() != null) {
            spot.setHasShade(dto.getHasShade());
        }

        if (dto.getAccessibleForDisabled() != null) {
            spot.setAccessibleForDisabled(
                    dto.getAccessibleForDisabled()
            );
        }

        if (dto.getActive() != null) {
            spot.setActive(dto.getActive());
        }

        if (dto.getPhotos() != null) {
            spot.setPhotos(dto.getPhotos());
        }
    }
}