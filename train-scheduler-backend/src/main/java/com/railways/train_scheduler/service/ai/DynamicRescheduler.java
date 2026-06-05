package com.railways.train_scheduler.service.ai;

import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.repository.PlanRepository;
import com.railways.train_scheduler.repository.TrainRepository;
import com.railways.train_scheduler.service.RoutingService;
import com.railways.train_scheduler.service.ReservationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DynamicRescheduler {

    private final PlanRepository planRepository;
    private final TrainRepository trainRepository;
    private final RoutingService routingService;
    private final ReservationService reservationService;

    public DynamicRescheduler(PlanRepository planRepository, 
                              TrainRepository trainRepository, 
                              RoutingService routingService,
                              ReservationService reservationService) {
        this.planRepository = planRepository;
        this.trainRepository = trainRepository;
        this.routingService = routingService;
        this.reservationService = reservationService;
    }

    // Triggered when a track breaks or severe delay occurs
    public void recalculateAffectedTrains(String blockedTrackId) {
        System.out.println("[AI Rescheduler] Recalculating due to blockage on " + blockedTrackId);
        
        List<Plan> allPlans = planRepository.findAll();
        List<Train> affectedTrains = new ArrayList<>();

        for (Plan plan : allPlans) {
            boolean isAffected = false;
            if (plan.getRoute() != null) {
                for (var seg : plan.getRoute()) {
                    if (seg.getSectionId().equals(blockedTrackId)) {
                        isAffected = true;
                        break;
                    }
                }
            }

            if (isAffected) {
                // Cancel future reservations for this plan
                reservationService.cancelReservationsForPlan(plan.getId());
                trainRepository.findById(plan.getTrainId()).ifPresent(affectedTrains::add);
                planRepository.deleteById(plan.getId());
            }
        }

        if (!affectedTrains.isEmpty()) {
            // Re-route only the affected trains, keeping non-affected trains' reservations intact
            routingService.computeRoutes(affectedTrains);
            System.out.println("[AI Rescheduler] Rerouted " + affectedTrains.size() + " affected trains.");
        }
    }
}
