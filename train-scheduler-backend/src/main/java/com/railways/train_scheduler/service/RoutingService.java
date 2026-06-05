package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.repository.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * RoutingService computes train routes, reserves tracks, and handles conflicts.
 */
@Service
public class RoutingService {

    private final PlanRepository planRepository;
    private final PathFinder pathFinder;
    private final ReservationService reservationService;
    private final ConflictResolutionService conflictResolutionService;

    public RoutingService(PlanRepository planRepository,
                          PathFinder pathFinder,
                          ReservationService reservationService,
                          ConflictResolutionService conflictResolutionService) {
        this.planRepository = planRepository;
        this.pathFinder = pathFinder;
        this.reservationService = reservationService;
        this.conflictResolutionService = conflictResolutionService;
    }

    private int getPriorityValue(String type) {
        if (type == null) return 0;
        switch (type) {
            case "EXPRESS": return 3;
            case "LOCAL": return 2;
            case "FREIGHT": return 1;
            default: return 0;
        }
    }

    /**
     * Computes routes for all trains and handles reservations and rerouting conflicts.
     */
    public List<Plan> computeRoutes(List<Train> trains) {
        // Sort trains: highest priority first, then earliest departure
        trains.sort((t1, t2) -> {
            int p1 = getPriorityValue(t1.getType());
            int p2 = getPriorityValue(t2.getType());
            if (p1 != p2) return Integer.compare(p2, p1); // descending
            
            java.time.LocalDateTime d1 = t1.getEarliestDeparture() != null ? java.time.LocalDateTime.parse(t1.getEarliestDeparture()) : java.time.LocalDateTime.now();
            java.time.LocalDateTime d2 = t2.getEarliestDeparture() != null ? java.time.LocalDateTime.parse(t2.getEarliestDeparture()) : java.time.LocalDateTime.now();
            return d1.compareTo(d2); // ascending
        });

        List<Plan> plans = new ArrayList<>();

        for (Train train : trains) {
            // Compute initial path using the PathFinder
            Plan plan = pathFinder.compute(train, null);
            plan.setTrainType(train.getType());

            // If no path found, hold train at source
            if (plan.getRoute() == null || plan.getRoute().isEmpty()) {
                System.out.println("No available path for train " + train.getId() + ". Train held at source.");
                plans.add(plan);
                continue;
            }

            try {
                // Attempt to reserve tracks along the route
                reservationService.reserveMany(plan.getId(), train.getId(), plan.getRoute());
                planRepository.save(plan);
                plans.add(plan);
            } catch (RuntimeException ex) {
                // Track capacity full → hold train at source
                System.out.println("Reservation conflict for train " + train.getId() + ": " + ex.getMessage());
                plan.setRoute(new ArrayList<>()); // Empty route signals train held
                plans.add(plan);
            }
        }

        // Resolve global conflicts after all plans computed
        List<Plan> conflictFreePlans = conflictResolutionService.resolveConflicts(plans);

        return conflictFreePlans;
    }
}
