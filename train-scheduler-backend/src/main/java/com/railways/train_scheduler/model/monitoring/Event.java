package com.railways.train_scheduler.model.monitoring;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    private String sectionId;
    private String eventType; 
    private LocalDateTime timestamp;
    private String description;
}
