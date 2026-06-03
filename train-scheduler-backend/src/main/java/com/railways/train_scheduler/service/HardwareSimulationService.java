package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.monitoring.LiveState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class HardwareSimulationService {

    private final LiveStateService liveStateService;
    private final Random random = new Random();

    // Mock data for our simulation
    private final List<String> simulatedTrains = Arrays.asList("T-101 (Shatabdi)", "T-202 (Rajdhani)", "T-303 (Duronto)");
    private final List<String> simulatedTracks = Arrays.asList("TRK-10", "TRK-15", "TRK-22", "TRK-45", "TRK-101");

    public HardwareSimulationService(LiveStateService liveStateService) {
        this.liveStateService = liveStateService;
    }

    // Run this simulator every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void simulateSensorPing() {
        String randomTrain = simulatedTrains.get(random.nextInt(simulatedTrains.size()));
        String randomTrack = simulatedTracks.get(random.nextInt(simulatedTracks.size()));
        
        LiveState ping = new LiveState();
        ping.setId(UUID.randomUUID().toString());
        ping.setTrainId(randomTrain);
        ping.setCurrentSectionId(randomTrack);
        
        // Randomize delay between 0 and 20 mins to show UI changes
        int randomDelay = random.nextInt(21);
        ping.setDelayInMinutes(randomDelay);
        
        ping.setExpectedArrival(LocalDateTime.now().plusMinutes(random.nextInt(60)));
        ping.setExpectedDeparture(LocalDateTime.now().plusMinutes(random.nextInt(60) + 10));

        System.out.println("[SIMULATOR] Sensor ping from " + randomTrain + " at " + randomTrack);
        
        // Send to LiveStateService which saves it and broadcasts over WebSockets
        liveStateService.updateLiveState(ping);
    }
}
