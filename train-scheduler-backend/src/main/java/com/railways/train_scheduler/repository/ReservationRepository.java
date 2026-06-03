package com.railways.train_scheduler.repository;

import com.railways.train_scheduler.model.scheduling.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    
    List<Reservation> findByTrackSectionIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        String trackSectionId, LocalDateTime end, LocalDateTime start);

    List<Reservation> findByPlanId(String planId);
}
