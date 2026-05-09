package org.example.backend_tunisiahub.Services.Accommodation;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Accommodation.Accommodation;
import org.example.backend_tunisiahub.Entities.Accommodation.UserHistory;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Accommodation.AccommodationRepository;
import org.example.backend_tunisiahub.Repositories.Accommodation.UserHistoryRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHistoryService implements IUserHistoryService {

    final UserHistoryRepository userHistoryRepository;
    final UserRepository userRepository;
    final AccommodationRepository accommodationRepository;
    @Override
    public void trackView(Long accommodationId, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return;

        Accommodation accommodation = accommodationRepository
                .findById(accommodationId).orElse(null);
        if (accommodation == null) return;

        // Avoid duplicate tracking for same accommodation
        if (userHistoryRepository.existsByUserAndAccommodationId(user, accommodationId))
            return;

        UserHistory history = new UserHistory();
        history.setUser(user);
        history.setAccommodation(accommodation);
        userHistoryRepository.save(history);
    }
    @Override
    public List<UserHistory> getUserHistory(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return List.of();
        return userHistoryRepository.findTop10ByUserOrderByViewedAtDesc(user);
    }
}