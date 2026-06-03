package com.railways.train_scheduler.model.scheduling;

import java.time.LocalDateTime;

public class RouteSegment {
    private String sectionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getters and setters
    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}
