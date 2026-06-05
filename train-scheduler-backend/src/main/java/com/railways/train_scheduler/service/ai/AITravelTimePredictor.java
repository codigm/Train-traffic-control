package com.railways.train_scheduler.service.ai;

import com.railways.train_scheduler.model.core.Track;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AITravelTimePredictor {

    // Simple heuristic to predict travel time based on distance, speed, weather, and time of day
    public int predictTravelTimeMinutes(Track track, LocalDateTime entryTime, String weatherCondition) {
        if (track == null) return 5;
        
        // Base travel time calculation
        int baseTravelTime = track.getTravelTime() > 0 ? track.getTravelTime() :
                (int) Math.max(1, Math.round((track.getDistanceKm() / (double) Math.max(1, track.getMaxSpeedKmph())) * 60));

        double multiplier = 1.0;

        // 1. Time of day congestion (Rush hours: 8-10 AM and 5-7 PM)
        int hour = entryTime.getHour();
        if ((hour >= 8 && hour <= 10) || (hour >= 17 && hour <= 19)) {
            multiplier += 0.2; // 20% slower during rush hour
        }

        // 2. Base congestion level of the track
        multiplier += track.getBaseCongestionLevel() * 0.5; // up to 50% slower if highly congested

        // 3. Weather Conditions
        if (weatherCondition != null && !weatherCondition.isEmpty()) {
            if (weatherCondition.contains("Fog") || weatherCondition.contains("Low Visibility")) {
                multiplier += 0.3; // 30% slower
            } else if (weatherCondition.contains("Rain") || weatherCondition.contains("Flooded")) {
                multiplier += 0.4;
            } else if (weatherCondition.contains("Wind")) {
                multiplier += 0.15;
            } else if (weatherCondition.contains("Snow")) {
                multiplier += 0.5;
            }
        }

        return (int) Math.ceil(baseTravelTime * multiplier);
    }
}
