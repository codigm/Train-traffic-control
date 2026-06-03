package com.railways.train_scheduler.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.railways.train_scheduler.model.monitoring.KPI;

@Repository
public interface KPIRepository extends MongoRepository<KPI, String> {
}
