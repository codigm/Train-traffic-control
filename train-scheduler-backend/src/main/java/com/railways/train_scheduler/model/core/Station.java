package com.railways.train_scheduler.model.core;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stations")
public class Station {

    @Id
    private String id;
    private String stationName;
    private int platformCapacity;

    public Station() {}

    public Station(String id, String stationName, int platformCapacity) {
        this.id = id;
        this.stationName = stationName;
        this.platformCapacity = platformCapacity;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public int getPlatformCapacity() { return platformCapacity; }
    public void setPlatformCapacity(int platformCapacity) { this.platformCapacity = platformCapacity; }

    @Override
    public String toString() {
        return "Station{" + "id='" + id + '\'' + ", name='" + stationName + '\'' + ", capacity=" + platformCapacity + '}';
    }
}
