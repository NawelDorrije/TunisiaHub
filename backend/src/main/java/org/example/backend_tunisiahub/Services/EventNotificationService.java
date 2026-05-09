package org.example.backend_tunisiahub.Services;



import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventNotificationService {

    private final EventRepository eventRepository;

    @Scheduled(fixedRate = 60000) // chaque minute
    public void checkEvents() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24h = now.plusDays(1);

        var events = eventRepository.findAll();

        for (var event : events) {
            if (event.getStartDate().isBefore(in24h)
                    && event.getStartDate().isAfter(now)) {

                System.out.println("EVENT SOON: " + event.getTitle());
                // ici tu peux envoyer email / websocket / push
            }
        }
    }
}
