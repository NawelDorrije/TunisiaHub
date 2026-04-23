package org.example.backend_tunisiahub.Repositories.Accommodation;

import jakarta.transaction.Transactional;
import org.example.backend_tunisiahub.Entities.Accommodation.UserHistory;
import org.example.backend_tunisiahub.Entities.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findTop10ByUserOrderByViewedAtDesc(User user);
    boolean existsByUserAndAccommodationId(User user, Long accommodationId);
    @Transactional
    void deleteByAccommodationId(Long accommodationId);

}