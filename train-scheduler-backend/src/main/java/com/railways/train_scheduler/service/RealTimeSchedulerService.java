package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.repository.PlanRepository;
import org.springframework.stereotype.Service;
import java.util.Objects;

import java.util.List;

@Service
public class RealTimeSchedulerService {

    private final RoutingService routingService;
    private final PlanRepository planRepository;

    public RealTimeSchedulerService(RoutingService routingService,
                                    PlanRepository planRepository) {
        this.routingService = routingService;
        this.planRepository = planRepository;
    }

    public List<Plan> scheduleTrains(List<Train> trains) {
        List<Plan> plans = routingService.computeRoutes(trains);
        planRepository.saveAll(Objects.requireNonNull(plans));
        return plans;
    }
}
