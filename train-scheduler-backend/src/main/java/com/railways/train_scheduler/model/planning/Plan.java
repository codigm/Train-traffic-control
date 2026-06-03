package com.railways.train_scheduler.model.planning;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "plans")
public class Plan {
    @Id
    private String id;
    private String trainId;
    private List<RouteSegment> route;
    private LocalDateTime createdAt;
    private String trainType;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTrainId() { return trainId; }
    public void setTrainId(String trainId) { this.trainId = trainId; }

    public List<RouteSegment> getRoute() { return route; }
    public void setRoute(List<RouteSegment> route) { this.route = route; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTrainType() { return trainType; }
    public void setTrainType(String trainType) { this.trainType = trainType; }
}
