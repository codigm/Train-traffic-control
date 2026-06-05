package com.railways.train_scheduler.repository;

import com.railways.train_scheduler.model.core.Track;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrackRepository extends MongoRepository<Track, String> {
    Optional<Track> findByFromStationAndToStation(String fromStation, String toStation);
}
