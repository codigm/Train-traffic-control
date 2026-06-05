package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.scheduling.Reservation;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.routing.RoutingConstraints;
import com.railways.train_scheduler.repository.TrackRepository;
import com.railways.train_scheduler.service.ai.AITravelTimePredictor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class TimedDijkstraPathFinder implements PathFinder {

    private final TrackRepository trackRepo;
    private final ReservationService reservationService;
    private final AITravelTimePredictor travelTimePredictor;

    public TimedDijkstraPathFinder(TrackRepository trackRepo, ReservationService reservationService, AITravelTimePredictor travelTimePredictor) {
        this.trackRepo = trackRepo;
        this.reservationService = reservationService;
        this.travelTimePredictor = travelTimePredictor;
    }

    @Override
    public Plan compute(Train train, RoutingConstraints constraints) {
        List<Track> edges = trackRepo.findAll();
        LocalDateTime startTime = train.getEarliestDeparture() != null ?
                LocalDateTime.parse(train.getEarliestDeparture()) : LocalDateTime.now();

        Plan plan = new Plan();
        plan.setTrainId(train.getId());
        plan.setTrainType(train.getType());

        // Segment splitting: Start -> MustStop1 -> MustStop2 -> End
        List<String> routeNodes = new ArrayList<>();
        routeNodes.add(train.getSource() != null ? train.getSource() : "");
        if (train.getMustStopStations() != null) {
            routeNodes.addAll(train.getMustStopStations());
        }
        routeNodes.add(train.getDestination() != null ? train.getDestination() : "");

        List<RouteSegment> finalSegments = new ArrayList<>();
        LocalDateTime cursor = startTime;

        Map<String, List<Track>> adjList = new HashMap<>();
        for (Track t : edges) {
            if (t.getFromStation() != null) {
                adjList.computeIfAbsent(t.getFromStation(), k -> new ArrayList<>()).add(t);
            }
        }

        boolean pathSuccess = true;

        for (int i = 0; i < routeNodes.size() - 1; i++) {
            String from = routeNodes.get(i);
            String to = routeNodes.get(i + 1);

            if (from.equals(to) || from.isEmpty() || to.isEmpty()) continue;

            List<RouteSegment> segments = dijkstraSegment(train, adjList, edges, from, to, cursor, null); // passing null for weather for now
            if (segments.isEmpty()) {
                pathSuccess = false;
                break;
            }
            finalSegments.addAll(segments);
            // cursor advances to the end of the last segment
            cursor = segments.get(segments.size() - 1).getEndTime();
        }

        if (!pathSuccess) {
            plan.setRoute(new ArrayList<>());
        } else {
            plan.setRoute(finalSegments);
        }
        plan.setCreatedAt(LocalDateTime.now());
        return plan;
    }

    private static class QueueNode implements Comparable<QueueNode> {
        String id;
        int timeElapsed;
        List<RouteSegment> pathSoFar;

        QueueNode(String id, int timeElapsed, List<RouteSegment> pathSoFar) {
            this.id = id;
            this.timeElapsed = timeElapsed;
            this.pathSoFar = new ArrayList<>(pathSoFar);
        }

        @Override
        public int compareTo(QueueNode o) {
            return Integer.compare(this.timeElapsed, o.timeElapsed);
        }
    }

    private List<RouteSegment> dijkstraSegment(Train train, Map<String, List<Track>> adjList, List<Track> edges, String source, String dest, LocalDateTime startTime, String weatherCondition) {
        Map<String, Integer> minArrivalTime = new HashMap<>();
        PriorityQueue<QueueNode> pq = new PriorityQueue<>();

        pq.add(new QueueNode(source, 0, new ArrayList<>()));
        minArrivalTime.put(source, 0);

        while (!pq.isEmpty()) {
            QueueNode uNode = pq.poll();
            String u = uNode.id;

            if (u.equals(dest)) {
                return uNode.pathSoFar;
            }

            if (uNode.timeElapsed > minArrivalTime.getOrDefault(u, Integer.MAX_VALUE)) {
                continue;
            }

            List<Track> neighbors = adjList.getOrDefault(u, Collections.emptyList());
            for (Track edge : neighbors) {
                if (!edge.isOperational()) continue; // Ignore broken tracks

                LocalDateTime arrivalAtU = startTime.plusMinutes(uNode.timeElapsed);
                int travelMinutes = travelTimePredictor.predictTravelTimeMinutes(edge, arrivalAtU, weatherCondition);
                travelMinutes *= getPriorityFactor(train, edge);

                // Wait vs Reroute: Calculate wait time if the track is currently occupied
                int waitMinutes = 0;
                LocalDateTime tentativeEnterTime = arrivalAtU;
                LocalDateTime tentativeExitTime = tentativeEnterTime.plusMinutes(travelMinutes);

                // Fast forward wait time until track is free
                int maxWaitScans = 12; // wait up to 1 hour (12 * 5 mins) before considering completely blocked
                boolean canReserve = false;
                for (int scan = 0; scan < maxWaitScans; scan++) {
                    if (reservationService.canReserve(train, edge, tentativeEnterTime, tentativeExitTime)) {
                        canReserve = true;
                        break;
                    }
                    tentativeEnterTime = tentativeEnterTime.plusMinutes(5);
                    tentativeExitTime = tentativeExitTime.plusMinutes(5);
                    waitMinutes += 5;
                }

                if (!canReserve) continue; // Skip this edge if we can't reserve it even after waiting

                int newTimeElapsed = uNode.timeElapsed + waitMinutes + travelMinutes;

                if (newTimeElapsed < minArrivalTime.getOrDefault(edge.getToStation(), Integer.MAX_VALUE)) {
                    minArrivalTime.put(edge.getToStation(), newTimeElapsed);
                    
                    RouteSegment seg = new RouteSegment();
                    seg.setSectionId(edge.getId());
                    seg.setStartTime(tentativeEnterTime); // includes wait time implicitly
                    seg.setEndTime(tentativeExitTime);

                    List<RouteSegment> newPath = new ArrayList<>(uNode.pathSoFar);
                    newPath.add(seg);
                    
                    pq.add(new QueueNode(edge.getToStation(), newTimeElapsed, newPath));
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private int getPriorityFactor(Train train, Track track) {
        if (train.getType() == null) return 1;
        switch (train.getType()) {
            case "EXPRESS": return track.getTrackType() == Track.TrackType.FREIGHT ? 3 : 1;
            case "LOCAL": return track.getTrackType() == Track.TrackType.FREIGHT ? 3 : 1;
            case "FREIGHT": return track.getTrackType() == Track.TrackType.SINGLE ? 3 : 1;
            default: return 1;
        }
    }
}
