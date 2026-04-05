package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.DTO.ActivityDTO;
import org.example.backend_tunisiahub.Entities.Camping.Mappers.ActivityMapper;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.ActivityRepository;
import org.example.backend_tunisiahub.Repositories.Camping.CampingRepository;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements IActivityService {

    private final ActivityRepository activityRepository;
    private final CampingRepository campingRepository;
    private final SpotRepository spotRepository;
    private final ActivityMapper activityMapper;

    public ActivityServiceImpl(ActivityRepository activityRepository,
                               CampingRepository campingRepository,
                               SpotRepository spotRepository,
                               ActivityMapper activityMapper) {
        this.activityRepository = activityRepository;
        this.campingRepository = campingRepository;
        this.spotRepository = spotRepository;
        this.activityMapper = activityMapper;
    }

    @Override
    public ActivityDTO createActivity(ActivityDTO dto) {
        Activity activity = new Activity();
        activityMapper.updateEntityFromDTO(dto, activity);

        Camping camping = campingRepository.findById(dto.getCampingId())
                .orElseThrow(() -> new RuntimeException("Camping not found with id: " + dto.getCampingId()));
        activity.setCamping(camping);

        if (dto.getSpotId() != null) {
            Spot spot = spotRepository.findById(dto.getSpotId())
                    .orElseThrow(() -> new RuntimeException("Spot not found with id: " + dto.getSpotId()));
            activity.setSpot(spot);
        }

        return activityMapper.toDTO(activityRepository.save(activity));
    }

    @Override
    public ActivityDTO updateActivity(Long id, ActivityDTO dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));

        activityMapper.updateEntityFromDTO(dto, activity);

        Camping camping = campingRepository.findById(dto.getCampingId())
                .orElseThrow(() -> new RuntimeException("Camping not found with id: " + dto.getCampingId()));
        activity.setCamping(camping);

        if (dto.getSpotId() != null) {
            Spot spot = spotRepository.findById(dto.getSpotId())
                    .orElseThrow(() -> new RuntimeException("Spot not found with id: " + dto.getSpotId()));
            activity.setSpot(spot);
        } else {
            activity.setSpot(null); // explicitly unlink spot if not provided
        }

        return activityMapper.toDTO(activityRepository.save(activity));
    }

    @Override
    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new RuntimeException("Activity not found with id: " + id);
        }
        activityRepository.deleteById(id);
    }

    @Override
    public ActivityDTO getActivityById(Long id) {
        return activityRepository.findById(id)
                .map(activityMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
    }

    @Override
    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findAll()
                .stream()
                .map(activityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityDTO> getActivitiesByCampingId(Long campingId) {
        return activityRepository.findByCampingId(campingId)
                .stream()
                .map(activityMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityDTO> getActivitiesBySpotId(Long spotId) {
        return activityRepository.findBySpotId(spotId)
                .stream()
                .map(activityMapper::toDTO)
                .collect(Collectors.toList());
    }
}