package com.railways.train_scheduler.model.routing;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class RoutingConstraints {

    private LocalDateTime earliestDeparture;  
    private Set<String> mustStopStations;     
    private boolean allowFreightTracks;       

    public RoutingConstraints() {
        this.mustStopStations = new HashSet<>();
        this.allowFreightTracks = false;
    }

    public LocalDateTime getEarliestDeparture() {
        return earliestDeparture;
    }

    public void setEarliestDeparture(LocalDateTime earliestDeparture) {
        this.earliestDeparture = earliestDeparture;
    }

    public Set<String> getMustStopStations() {
        return mustStopStations;
    }

    public void setMustStopStations(Set<String> mustStopStations) {
        this.mustStopStations = mustStopStations;
    }

    public boolean isAllowFreightTracks() {
        return allowFreightTracks;
    }

    public void setAllowFreightTracks(boolean allowFreightTracks) {
        this.allowFreightTracks = allowFreightTracks;
    }
}
