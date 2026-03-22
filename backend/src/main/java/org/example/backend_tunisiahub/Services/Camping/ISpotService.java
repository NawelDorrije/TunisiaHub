package org.example.backend_tunisiahub.Services.Camping;

import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Spot;

import java.util.List;

public interface ISpotService {

    List<Spot> retrieveAllSpots();

    Spot retrieveSpot(Long id);

    Spot addSpot(SpotDTO spotDTO);

    void deleteSpot(Long id);

    Spot updateSpot(Long id, SpotDTO spotDTO);
}
