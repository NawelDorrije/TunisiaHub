package org.example.backend_tunisiahub.Repositories.Event;

import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTitleContainingIgnoreCase(String title);

    List<Event> findByType(EventType type);

    long countByStartDateBetween(LocalDateTime start, LocalDateTime end);
}
