package org.example.backend_tunisiahub.Controllers.Event;
import lombok.RequiredArgsConstructor;
import org.example.backend_tunisiahub.Entities.Event.Event;
import org.example.backend_tunisiahub.Services.Event.IEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;


    @GetMapping("/all")
    public List<Event> getAll() {
        return eventService.retrieveAllEvents();
    }

    @GetMapping("/{id}")
    public Event getById(@PathVariable Long id) {
        return eventService.retrieveEvent(id);
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

    @GetMapping("/search")
    public List<Event> search(@RequestParam String title) {
        return eventService.searchByTitle(title);
    }

}