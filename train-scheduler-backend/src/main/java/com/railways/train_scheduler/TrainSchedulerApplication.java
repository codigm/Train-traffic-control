package com.railways.train_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrainSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainSchedulerApplication.class, args);
    }

}
