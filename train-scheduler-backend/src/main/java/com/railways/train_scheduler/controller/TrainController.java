package com.railways.train_scheduler.controller;

import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.repository.TrainRepository;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/trains")
public class TrainController {

    private final TrainRepository trainRepository;

    public TrainController(TrainRepository trainRepository) {
        this.trainRepository = trainRepository;
    }

    @GetMapping
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @PostMapping
    public Train addTrain(@RequestBody @NonNull Train train) {
        return trainRepository.save(train);
    }
    // PUT: Update an existing train
    @PutMapping("/{id}")
    public Train updateTrain(@PathVariable @NonNull String id, @RequestBody Train trainDetails) {
    Train train = trainRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Train not found"));
    train.setTrainName(trainDetails.getTrainName());
    train.setSource(trainDetails.getSource());
    train.setDestination(trainDetails.getDestination());
    train.setDepartureTime(trainDetails.getDepartureTime());
    train.setArrivalTime(trainDetails.getArrivalTime());
    return trainRepository.save(train);
}
     // DELETE: Remove a train
    @DeleteMapping("/{id}")
    public void deleteTrain(@PathVariable @NonNull String id) {
        trainRepository.deleteById(id);
    }
    // DELETE: Remove all trains
    @DeleteMapping
    public void deleteAllTrains() {
    trainRepository.deleteAll();
}
    
    // 🔎 Search by source
    @GetMapping("/source/{source}")
    public List<Train> getTrainsBySource(@PathVariable String source) {
        return trainRepository.findBySource(source);
    }

    // 🔎 Search by destination
    @GetMapping("/destination/{destination}")
    public List<Train> getTrainsByDestination(@PathVariable String destination) {
        return trainRepository.findByDestination(destination);
    }

    // 🔎 Search by source and destination
    @GetMapping("/search")
    public List<Train> searchTrains(
            @RequestParam String source,
            @RequestParam String destination) {
        return trainRepository.findBySourceAndDestination(source, destination);
    }
}
