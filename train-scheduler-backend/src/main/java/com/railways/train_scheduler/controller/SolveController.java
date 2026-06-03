package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.service.RealTimeSchedulerService;
import com.railways.train_scheduler.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solve")
public class SolveController {

    @Autowired
    private RealTimeSchedulerService schedulerService;

    @Autowired
    private TrainRepository trainRepository;

    @PostMapping
    public ResponseEntity<?> solve() {
        try {
            List<Train> trains = trainRepository.findAll();
            List<Plan> plans = schedulerService.scheduleTrains(trains);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            e.printStackTrace(); // prints the full stack trace to console
            return ResponseEntity.status(500).body("Error while scheduling: " + e.getMessage());
        }
    }
}
