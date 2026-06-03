package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.routing.RoutingConstraints;

public interface PathFinder {
    Plan compute(Train train, RoutingConstraints constraints);
}
