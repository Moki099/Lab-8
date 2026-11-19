package kz.iitu.registration.controller;

import kz.iitu.registration.model.Event;
import kz.iitu.registration.service.EventService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final EventService eventService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.course}")
    private String course;

    @Value("${app.semester}")
    private String semester;

    public RegistrationController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", eventService.getWelcomeMessage());
    }

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of(
                "applicationName", applicationName,
                "course", course,
                "semester", semester
        );
    }

    @GetMapping("/events")
    public List<Event> getEvents(@RequestParam(required = false) String title) {
        return title == null ? eventService.getAllEvents() : eventService.searchEvents(title);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Event with id " + id + " was not found")));
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody(required = false) Event request) {
        if (!hasValidFields(request)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title and location are required"));
        }

        Event created = eventService.addEvent(request.getTitle(), request.getLocation());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestBody(required = false) Event request) {
        if (!hasValidFields(request)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Title and location are required"));
        }

        return eventService.updateEvent(id, request.getTitle(), request.getLocation())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Event not found")));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        if (!eventService.deleteEvent(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Event not found"));
        }

        return ResponseEntity.noContent().build();
    }

    private boolean hasValidFields(Event request) {
        return request != null
                && request.getTitle() != null && !request.getTitle().isBlank()
                && request.getLocation() != null && !request.getLocation().isBlank();
    }
}
