package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.repository.TrainRepository;
import com.railways.train_scheduler.service.RealTimeSchedulerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    private final RealTimeSchedulerService scheduler;
    private final TrainRepository trainRepo;

    public TestController(RealTimeSchedulerService scheduler, TrainRepository trainRepo) {
        this.scheduler = scheduler;
        this.trainRepo = trainRepo;
    }

    @GetMapping("/schedule")
public ResponseEntity<?> testSchedule() {
    try {
        List<Train> trains = trainRepo.findAll();
        List<Plan> plans = scheduler.scheduleTrains(trains);
        return ResponseEntity.ok(plans);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}
}
