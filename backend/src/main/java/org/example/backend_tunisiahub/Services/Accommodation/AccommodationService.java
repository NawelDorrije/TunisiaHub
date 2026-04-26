package org.example.backend_tunisiahub.Services.Accommodation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.UserHistoryRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccommodationService implements IAccommodationService {

    final AccommodationRepository accommodationRepository;
    final UserHistoryRepository historyRepository;
    @Override
    public List<Accommodation> retrieveAllAccommodations() {
        return accommodationRepository.findAll();
    }

    @Override
    public List<Accommodation> retrieveFilteredAccommodations(String type, Double minPrice, Double maxPrice, Integer minCapacity) {
        Specification<Accommodation> spec = Specification.where(null);

        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("type").as(String.class)), type.toLowerCase()));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price").as(Double.class), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price").as(Double.class), maxPrice));
        }

        if (minCapacity != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacite").as(Integer.class), minCapacity));
        }

        return accommodationRepository.findAll(spec);
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
    @Transactional
    public void removeAccommodation(Long accommodationId) {
        historyRepository.deleteByAccommodationId(accommodationId);

        accommodationRepository.deleteById(accommodationId);
    }
    @Override
    public Accommodation modifyAccommodation(Accommodation accommodation) {
        return accommodationRepository.save(accommodation);
    }

}