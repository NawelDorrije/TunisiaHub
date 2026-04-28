package org.example.backend_tunisiahub.Controllers.Event;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Entities.Event.EventType;
import org.example.backend_tunisiahub.Services.Event.FacebookPublisherService;
import org.example.backend_tunisiahub.Services.Event.IEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;
    private final FacebookPublisherService facebookPublisherService;


    @GetMapping("/all")
    public List<Event> getAll() {
        return eventService.retrieveAllEvents();
    }

    @GetMapping("/{id}")
    public Event getById(@PathVariable Long id) {
        return eventService.retrieveEvent(id);
    }

    @GetMapping("/search")
    public List<Event> search(@RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "title", required = false) String title) {
        String effectiveKeyword = (keyword != null && !keyword.isBlank()) ? keyword : title;
        return eventService.searchByKeyword(effectiveKeyword);
    }

    @GetMapping("/filter")
    public List<Event> filterByType(@RequestParam String type) {
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return eventService.retrieveAllEvents();
        }
        try {
            EventType eventType = EventType.valueOf(type.trim().toUpperCase());
            return eventService.filterByType(eventType);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event type: " + type);
        }
    }

    @PostMapping("/add")
    public Event add(@RequestBody Event event) {
        return eventService.addEvent(event);
    }

    @PutMapping("/update")
    public Event update(@RequestBody Event event) {
        return eventService.modifyEvent(event);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

    @PostMapping("/{id}/publish-facebook")
    public ResponseEntity<String> publishEventToFacebook(@PathVariable Long id) {
        String result = facebookPublisherService.publishEventToPage(id);
        return ResponseEntity.ok(result);
    }

}
