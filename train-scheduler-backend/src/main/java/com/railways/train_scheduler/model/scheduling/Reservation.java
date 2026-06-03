package com.railways.train_scheduler.model.scheduling;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reservations")
public class Reservation {
    @Id
    private String id;
    private String trackSectionId;
    private String trainId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String planId;

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTrackSectionId() { return trackSectionId; }
    public void setTrackSectionId(String trackSectionId) { this.trackSectionId = trackSectionId; }

    public String getTrainId() { return trainId; }
    public void setTrainId(String trainId) { this.trainId = trainId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
}
