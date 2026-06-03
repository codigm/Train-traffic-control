package com.railways.train_scheduler.model.core;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tracks")
public class Track {

    public enum TrackType { SINGLE, DOUBLE, FREIGHT }

    @Id
    private String id;
    private String fromStation;
    private String toStation;
    private int capacity;      // 1 per track
    private int distanceKm;
    private int travelTime;
    private int maxSpeedKmph;
    private TrackType trackType;
    private boolean isOperational = true;

    // Constructors
    public Track() {}
    public Track(String id, String fromStation, String toStation, int capacity, int distanceKm, int travelTime, int maxSpeedKmph, TrackType trackType) {
        this.id = id; this.fromStation = fromStation; this.toStation = toStation;
        this.capacity = capacity; this.distanceKm = distanceKm; this.travelTime = travelTime;
        this.maxSpeedKmph = maxSpeedKmph; this.trackType = trackType;
        this.isOperational = true;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }

    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getDistanceKm() { return distanceKm; }
    public void setDistanceKm(int distanceKm) { this.distanceKm = distanceKm; }

    public int getTravelTime() { return travelTime; }
    public void setTravelTime(int travelTime) { this.travelTime = travelTime; }

    public int getMaxSpeedKmph() { return maxSpeedKmph; }
    public void setMaxSpeedKmph(int maxSpeedKmph) { this.maxSpeedKmph = maxSpeedKmph; }

    public TrackType getTrackType() { return trackType; }
    public void setTrackType(TrackType trackType) { this.trackType = trackType; }

    public boolean isOperational() { return isOperational; }
    public void setOperational(boolean operational) { isOperational = operational; }

    @Override
    public String toString() {
        return "Track{" + id + ", " + fromStation + "->" + toStation + ", type=" + trackType + ", operational=" + isOperational + "}";
    }
}
