package org.example.backend_tunisiahub.Services.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Entities.Camping.DTO.SpotDTO;
import org.example.backend_tunisiahub.Entities.Camping.Spot;
import org.example.backend_tunisiahub.Repositories.Camping.CampingRepository;
import org.example.backend_tunisiahub.Repositories.Camping.SpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotService implements ISpotService {

    private final SpotRepository spotRepository;
    private final CampingRepository campingRepository;


    @Override
    public List<Spot> retrieveAllSpots() {
        return spotRepository.findAll();
    }

    @Override
    public Spot retrieveSpot(Long id) {
        return spotRepository.findById(id).orElse(null);
    }

    @Override
    public Spot addSpot(SpotDTO dto) {

        Spot spot = new Spot();

        spot.setNumber(dto.number);
        spot.setSize(dto.size);
        spot.setAvailability(dto.availability);
        spot.setPrice(dto.price);
        spot.setMaxCapacity(dto.maxCapacity);

        Camping camping = campingRepository
                .findById(dto.campingId)
                .orElseThrow(() -> new RuntimeException("Camping not found"));

        spot.setCamping(camping);

        return spotRepository.save(spot);
    }

    @Override
    public void deleteSpot(Long id) {
        spotRepository.deleteById(id);
    }

    @Override
    public Spot updateSpot(Long id, SpotDTO dto) {

        Spot spot = spotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Spot not found"));

        spot.setNumber(dto.number);
        spot.setSize(dto.size);
        spot.setAvailability(dto.availability);
        spot.setPrice(dto.price);
        spot.setMaxCapacity(dto.maxCapacity);

        Camping camping = campingRepository
                .findById(dto.campingId)
                .orElseThrow(() -> new RuntimeException("Camping not found"));

        spot.setCamping(camping);

        return spotRepository.save(spot);
    }

}
