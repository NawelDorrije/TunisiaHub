package org.example.backend_tunisiahub.Services.Accommodation;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Controllers.Accommodation.AccommodationStatsDTO;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.AccommodationReview;
import org.example.backend_tunisiahub.Entities.Reservation;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.ReviewRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.UserHistoryRepository;
import org.example.backend_tunisiahub.Repositories.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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