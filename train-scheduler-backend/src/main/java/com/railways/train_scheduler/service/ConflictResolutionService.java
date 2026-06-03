package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConflictResolutionService {

    public List<Plan> resolveConflicts(List<Plan> plans) {
        plans.sort((p1,p2)->getPriority(p2)-getPriority(p1));
        return plans;
    }

    private int getPriority(Plan plan){
        switch(plan.getTrainType()){
            case "EXPRESS": return 3;
            case "LOCAL": return 2;
            case "FREIGHT": return 1;
            default: return 0;
        }
    }
}
