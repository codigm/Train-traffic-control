package com.railways.train_scheduler.model.monitoring;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "kpis")
public class KPI {
    @Id
    private String id;
    private double punctualityPercentage;
    private double averageDelayMinutes;
    private double throughput; 
    private double trackUtilization; 
}
