package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;

import java.util.List;

public interface IActivityService {
    ActivityDTO createActivity(ActivityDTO dto);
    ActivityDTO updateActivity(Long id, ActivityDTO dto);
    void deleteActivity(Long id);
    ActivityDTO getActivityById(Long id);
    List<ActivityDTO> getAllActivities();
    List<ActivityDTO> getActivitiesByCampingId(Long campingId);
    List<ActivityDTO> getActivitiesBySpotId(Long spotId);
}