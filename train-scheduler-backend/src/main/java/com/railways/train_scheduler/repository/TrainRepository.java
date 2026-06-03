package com.railways.train_scheduler.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.railways.train_scheduler.model.core.Train;

@Repository
public interface TrainRepository extends MongoRepository<Train, String> {
    // Extra custom queries can be added here later
     List<Train> findBySource(String source);
    List<Train> findByDestination(String destination);
    List<Train> findBySourceAndDestination(String source, String destination);
}
