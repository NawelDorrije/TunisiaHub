package org.example.backend_tunisiahub.Services.Camping;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Camping.Camping;
import org.example.backend_tunisiahub.Repositories.Camping.CampingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampingService implements ICampingService {

    private final CampingRepository campingRepository;

    @Override
    public List<Camping> retrieveAllCampings() {
        return campingRepository.findAll();
    }

    @Override
    public Camping retrieveCamping(Long id) {
        return campingRepository.findById(id).orElse(null);
    }

    @Override
    public Camping addCamping(Camping camping) {
        return campingRepository.save(camping);
    }

    @Override
    public void deleteCamping(Long id) {
        campingRepository.deleteById(id);
    }

    @Override
    public Camping modifyCamping(Camping camping) {
        return campingRepository.save(camping);
    }
}
