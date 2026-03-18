package org.example.backend_tunisiahub.Services.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotService implements ISpotService {

    private final SpotRepository spotRepository;

    @Override
    public List<Spot> retrieveAllSpots() {
        return spotRepository.findAll();
    }

    @Override
    public Spot retrieveSpot(Long id) {
        return spotRepository.findById(id).orElse(null);
    }

    @Override
    public Spot addSpot(Spot spot) {
        return spotRepository.save(spot);
    }

    @Override
    public void deleteSpot(Long id) {
        spotRepository.deleteById(id);
    }

    @Override
    public Spot modifySpot(Spot spot) {
        return spotRepository.save(spot);
    }
}
