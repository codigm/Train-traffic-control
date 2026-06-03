package com.railways.train_scheduler.model.monitoring;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Document(collection = "liveState")
public class LiveState {
    @Id
    private String id;
    private String trainId;
    private String currentSectionId;
    private LocalDateTime expectedArrival;
    private LocalDateTime expectedDeparture;
    private int delayInMinutes;
}
