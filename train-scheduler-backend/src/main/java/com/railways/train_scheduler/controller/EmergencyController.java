package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.monitoring.Event;
import com.railways.train_scheduler.repository.EventRepository;
import com.railways.train_scheduler.service.EmergencyHandlingService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/emergency")
public class EmergencyController {
    
    private final EmergencyHandlingService emergencyHandlingService;
    private final EventRepository eventRepository;

    public EmergencyController(EmergencyHandlingService emergencyHandlingService, EventRepository eventRepository) {
        this.emergencyHandlingService = emergencyHandlingService;
        this.eventRepository = eventRepository;
    }

    @PostMapping("/report")
    public ResponseEntity<Event> reportEmergency(@RequestBody Map<String, String> payload) {
        String trackId = payload.get("trackId");
        String eventType = payload.get("eventType");
        String description = payload.get("description");

        if (trackId == null || eventType == null) {
            return ResponseEntity.badRequest().build();
        }

        Event event = emergencyHandlingService.reportEmergency(trackId, eventType, description);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/resolve/{trackId}")
    public ResponseEntity<Event> resolveEmergency(@PathVariable @NonNull String trackId) {
        Event event = emergencyHandlingService.resolveEmergency(trackId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/events")
    public ResponseEntity<List<Event>> getAllEvents() {
        return ResponseEntity.ok(eventRepository.findAll());
    }
}
