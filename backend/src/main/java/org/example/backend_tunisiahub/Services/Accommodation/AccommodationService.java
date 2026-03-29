package org.example.backend_tunisiahub.Services.Accommodation;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccommodationService implements IAccommodationService {

    final AccommodationRepository accommodationRepository;

    @Override
    public List<Accommodation> retrieveAllAccommodations() {
        return accommodationRepository.findAll();
    }
    @Override
    public Accommodation retrieveAccommodation(Long accommodationId) {
        return accommodationRepository.findById(accommodationId).get();
    }
    @Override
    public Accommodation addAccommodation(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }
    @Override
    public void removeAccommodation(Long accommodationId) {
        accommodationRepository.deleteById(accommodationId);
    }
    @Override
    public Accommodation modifyAccommodation(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }
}