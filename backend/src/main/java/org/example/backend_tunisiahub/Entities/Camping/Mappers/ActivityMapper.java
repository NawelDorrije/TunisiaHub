package org.example.backend_tunisiahub.Entities.Camping.Mappers;


import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    /**
     * Entity → DTO (for responses)
     */
    public ActivityDTO toDTO(Activity activity) {
        if (activity == null) return null;

        return ActivityDTO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .description(activity.getDescription())
                .price(activity.getPrice())
                .duration(activity.getDuration())
                .active(activity.getActive())
                .campingId(activity.getCamping() != null ? activity.getCamping().getId() : null)
                .campingName(activity.getCamping() != null ? activity.getCamping().getName() : null)
                .spotId(activity.getSpot() != null ? activity.getSpot().getId() : null)
                .spotName(activity.getSpot() != null ? activity.getSpot().getName() : null)
                .build();
    }

    /**
     * Apply DTO fields onto an existing entity (for update)
     * Camping and Spot are resolved in the service — not set here
     */
    public void updateEntityFromDTO(ActivityDTO dto, Activity activity) {
        activity.setName(dto.getName());
        activity.setDescription(dto.getDescription());
        activity.setPrice(dto.getPrice());
        activity.setDuration(dto.getDuration());
        activity.setActive(dto.getActive() != null ? dto.getActive() : activity.getActive());
    }
}