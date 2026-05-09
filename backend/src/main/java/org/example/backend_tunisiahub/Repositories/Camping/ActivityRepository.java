package org.example.backend_tunisiahub.Repositories.Camping;

import org.example.backend_tunisiahub.Entities.Camping.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByCampingId(Long campingId);
    List<Activity> findBySpotId(Long spotId);
}