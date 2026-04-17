package org.example.backend_tunisiahub.Services.Event;

import org.example.backend_tunisiahub.Entities.Event.Event;

import java.util.List;

public interface IEventService {

    List<Event> retrieveAllEvents();

    Event retrieveEvent(Long id);

    Event addEvent(Event event);

    void deleteEvent(Long id);

    Event modifyEvent(Event event);

    List<Event> searchByTitle(String title);
}