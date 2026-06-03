package com.railways.train_scheduler.model.core;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "trains")
public class Train {

    @Id
    private String id;
    private String trainName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int priority;             // Express > Local > Freight
    private List<String> mustStopStations;
    private String type;              // EXPRESS, LOCAL, FREIGHT
    private String earliestDeparture; // For rerouting/waiting logic

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { this.trainName = trainName; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public List<String> getMustStopStations() { return mustStopStations; }
    public void setMustStopStations(List<String> mustStopStations) { this.mustStopStations = mustStopStations; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEarliestDeparture() { return earliestDeparture; }
    public void setEarliestDeparture(String earliestDeparture) { this.earliestDeparture = earliestDeparture; }
}
