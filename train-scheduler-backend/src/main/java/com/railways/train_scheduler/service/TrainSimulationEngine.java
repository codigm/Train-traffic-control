package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import com.railways.train_scheduler.model.monitoring.LiveState;
import com.railways.train_scheduler.repository.PlanRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TrainSimulationEngine {

    private final PlanRepository planRepository;
    private final LiveStateService liveStateService;

    // Simulation clock starts now
    private LocalDateTime currentSimulationTime = LocalDateTime.now();

    public TrainSimulationEngine(PlanRepository planRepository, LiveStateService liveStateService) {
        this.planRepository = planRepository;
        this.liveStateService = liveStateService;
    }

    // Tick the simulation clock forward 1 minute every 5 real seconds
    @Scheduled(fixedRate = 5000)
    public void tickSimulation() {
        currentSimulationTime = currentSimulationTime.plusMinutes(1);
        
        List<Plan> activePlans = planRepository.findAll();
        
        for (Plan plan : activePlans) {
            if (plan.getRoute() == null || plan.getRoute().isEmpty()) continue;

            // Find where the train is at currentSimulationTime
            RouteSegment currentSegment = null;
            for (RouteSegment seg : plan.getRoute()) {
                if (!currentSimulationTime.isBefore(seg.getStartTime()) && currentSimulationTime.isBefore(seg.getEndTime())) {
                    currentSegment = seg;
                    break;
                }
            }

            if (currentSegment != null) {
                // Emit LiveState update
                LiveState ping = new LiveState();
                ping.setId(UUID.randomUUID().toString());
                ping.setTrainId(plan.getTrainId());
                ping.setCurrentSectionId(currentSegment.getSectionId());
                ping.setExpectedArrival(currentSegment.getEndTime());
                ping.setExpectedDeparture(currentSegment.getEndTime());
                ping.setDelayInMinutes(0); // If it's matching the plan perfectly

                liveStateService.updateLiveState(ping);
            }
        }
    }

    public LocalDateTime getCurrentSimulationTime() {
        return currentSimulationTime;
    }
}
