package org.example.backend_tunisiahub.Services.Event;

import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.EventType;
import org.example.backend_tunisiahub.Entities.User.RoleUser;
import org.example.backend_tunisiahub.Entities.User.User;
import org.example.backend_tunisiahub.Repositories.Event.EventRepository;
import org.example.backend_tunisiahub.Repositories.User.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EventService implements IEventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    @Value("${app.public-base-url:http://localhost:8089}")
    private String publicBaseUrl;
    private static final String DEFAULT_LOCAL_BASE_URL = "http://localhost:8089";

    @Override
    public List<Event> retrieveAllEvents() {
        List<Event> events = eventRepository.findAll();
        events.forEach(this::normalizeEventImageUrl);
        return events;
    }

    @Override
    public Event retrieveEvent(Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        normalizeEventImageUrl(event);
        return event;
    }

    @Override
    public Event addEvent(Event event) {
        attachEventCreator(event);
        return eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public Event modifyEvent(Event event) {
        if (event.getId() != null) {
            Event existing = eventRepository.findById(event.getId()).orElse(null);
            if (existing != null && existing.getCreatedBy() != null) {
                event.setCreatedBy(existing.getCreatedBy());
            } else {
                attachEventCreator(event);
            }
        } else {
            attachEventCreator(event);
        }
        return eventRepository.save(event);
    }

    @Override
    public List<Event> searchByTitle(String title) {
        List<Event> events = eventRepository.findByTitleContainingIgnoreCase(title);
        events.forEach(this::normalizeEventImageUrl);
        return events;
    }

    @Override
    public List<Event> searchByKeyword(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (q.isBlank()) {
            return retrieveAllEvents();
        }

        List<Event> events = eventRepository.findAll().stream()
                .filter(event -> containsIgnoreCase(event.getTitle(), q)
                        || containsIgnoreCase(event.getDescription(), q)
                        || containsIgnoreCase(event.getLieu(), q)
                        || containsIgnoreCase(event.getType() == null ? null : event.getType().name(), q))
                .toList();

        events.forEach(this::normalizeEventImageUrl);
        return events;
    }

    @Override
    public List<Event> filterByType(EventType type) {
        if (type == null) {
            return retrieveAllEvents();
        }

        List<Event> events = eventRepository.findByType(type);
        events.forEach(this::normalizeEventImageUrl);
        return events;
    }

    private void normalizeEventImageUrl(Event event) {
        if (event == null || event.getImage() == null || event.getImage().isBlank()) {
            return;
        }

        String image = event.getImage().trim();
        int uploadsIndex = image.indexOf("/uploads/events/");
        if (uploadsIndex < 0) {
            return;
        }

        String uploadsPath = image.substring(uploadsIndex);
        event.setImage(resolveStablePublicBaseUrl() + uploadsPath);
    }

    private String resolveStablePublicBaseUrl() {
        String normalized = stripTrailingSlash(publicBaseUrl);
        try {
            URI uri = URI.create(normalized);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return DEFAULT_LOCAL_BASE_URL;
            }
            String lowerHost = host.toLowerCase();
            if (lowerHost.endsWith("trycloudflare.com")) {
                return DEFAULT_LOCAL_BASE_URL;
            }
            if (lowerHost.endsWith("ngrok-free.app")
                    || lowerHost.endsWith("ngrok.io")
                    || lowerHost.endsWith("ngrok.app")) {
                return DEFAULT_LOCAL_BASE_URL;
            }
            return normalized;
        } catch (Exception ex) {
            return DEFAULT_LOCAL_BASE_URL;
        }
    }

    private String stripTrailingSlash(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return DEFAULT_LOCAL_BASE_URL;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private boolean containsIgnoreCase(String source, String queryLowerCase) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(queryLowerCase);
    }

    private void attachEventCreator(Event event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        String email = auth.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            return;
        }

        User user = userRepository.findByEmail(email);
        if (user == null || user.getRole() != RoleUser.ADMIN) {
            return;
        }

        event.setCreatedBy(user);
    }
}
