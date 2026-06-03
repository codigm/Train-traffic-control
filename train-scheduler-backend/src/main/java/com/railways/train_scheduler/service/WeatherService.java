package com.railways.train_scheduler.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class WeatherService {

    private final EmergencyHandlingService emergencyHandlingService;
    private final Random random = new Random();

    private final List<String> simulatedTracks = Arrays.asList("TRK-10", "TRK-15", "TRK-22", "TRK-45", "TRK-101");
    private final List<String> weatherEvents = Arrays.asList(
            "Dense Fog - Visibility Low", 
            "Heavy Rain - Tracks Flooded", 
            "High Wind - Speed Restriction", 
            "Snowstorm - Points Frozen"
    );

    public WeatherService(EmergencyHandlingService emergencyHandlingService) {
        this.emergencyHandlingService = emergencyHandlingService;
    }

    // Run this simulator every 45 seconds to sporadically inject weather emergencies
    @Scheduled(fixedRate = 45000)
    public void generateRandomWeatherEvent() {
        // Only a 30% chance to generate bad weather to avoid spamming the system
        if (random.nextInt(100) > 30) {
            return;
        }

        String randomTrack = simulatedTracks.get(random.nextInt(simulatedTracks.size()));
        String weather = weatherEvents.get(random.nextInt(weatherEvents.size()));

        System.out.println("[WEATHER ALERT] " + weather + " at " + randomTrack + ". System adapting limits.");

        // Automatically trigger an emergency event for this track
        emergencyHandlingService.reportEmergency(
                java.util.Objects.requireNonNull(randomTrack), 
                "SEVERE_WEATHER", 
                weather + ". Speed restrictions automatically applied to " + randomTrack
        );
    }
}
