package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.monitoring.LiveState;
import com.railways.train_scheduler.repository.LiveStateRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
@Service
public class LiveStateService {
    
    private final LiveStateRepository liveStateRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public LiveStateService(LiveStateRepository liveStateRepository, SimpMessagingTemplate messagingTemplate) {
        this.liveStateRepository = liveStateRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public LiveState updateLiveState(@NonNull LiveState state) {
        LiveState savedState = liveStateRepository.save(state);
        messagingTemplate.convertAndSend("/topic/livestate", savedState);
        return savedState;
    }

    public List<LiveState> getAllLiveStates() {
        return liveStateRepository.findAll();
    }

    public Optional<LiveState> getLiveStateById(@NonNull String id) {
        return liveStateRepository.findById(id);
    }

    public void deleteLiveState(@NonNull String id) {
        liveStateRepository.deleteById(id);
    }
}
