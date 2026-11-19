package kz.iitu.registration.service;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public String getWelcomeMessage() {
        return "Welcome to the Event Registration API";
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> searchEvents(String title) {
        return eventRepository.findByTitleContainingIgnoreCase(title);
    }

    public Event addEvent(String title, String location) {
        return eventRepository.save(new Event(title, location));
    }

    public Optional<Event> updateEvent(Long id, String title, String location) {
        return eventRepository.findById(id).map(event -> {
            event.setTitle(title);
            event.setLocation(location);
            return eventRepository.save(event);
        });
    }

    public boolean deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            return false;
        }
        eventRepository.deleteById(id);
        return true;
    }
}
