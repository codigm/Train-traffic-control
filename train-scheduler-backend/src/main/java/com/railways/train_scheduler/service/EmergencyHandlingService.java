package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.model.monitoring.Event;
import com.railways.train_scheduler.repository.EventRepository;
import com.railways.train_scheduler.repository.TrackRepository;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmergencyHandlingService {
    
    private final TrackRepository trackRepository;
    private final EventRepository eventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.railways.train_scheduler.service.ai.DynamicRescheduler dynamicRescheduler;

    public EmergencyHandlingService(TrackRepository trackRepository, EventRepository eventRepository, SimpMessagingTemplate messagingTemplate, com.railways.train_scheduler.service.ai.DynamicRescheduler dynamicRescheduler) {
        this.trackRepository = trackRepository;
        this.eventRepository = eventRepository;
        this.messagingTemplate = messagingTemplate;
        this.dynamicRescheduler = dynamicRescheduler;
    }

    public Event reportEmergency(@NonNull String trackId, String eventType, String description) {
        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        
        if (optionalTrack.isPresent()) {
            Track track = optionalTrack.get();
            // Don't shut down the track completely if it's just weather, speed restrictions are handled by AI
            if (!eventType.equals("SEVERE_WEATHER")) {
                track.setOperational(false);
            }
            trackRepository.save(track);
        }

        Event event = new Event();
        event.setSectionId(trackId);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setTimestamp(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        messagingTemplate.convertAndSend("/topic/emergencies", savedEvent);
        
        // Trigger AI dynamic recalculation for any trains scheduled to pass this broken track
        dynamicRescheduler.recalculateAffectedTrains(trackId);
        
        return savedEvent;
    }

    public Event resolveEmergency(@NonNull String trackId) {
        Optional<Track> optionalTrack = trackRepository.findById(trackId);
        
        if (optionalTrack.isPresent()) {
            Track track = optionalTrack.get();
            track.setOperational(true);
            trackRepository.save(track);
        }

        Event event = new Event();
        event.setSectionId(trackId);
        event.setEventType("EMERGENCY_RESOLVED");
        event.setDescription("Emergency resolved on track " + trackId);
        event.setTimestamp(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        messagingTemplate.convertAndSend("/topic/emergencies", savedEvent);
        return savedEvent;
    }
}
