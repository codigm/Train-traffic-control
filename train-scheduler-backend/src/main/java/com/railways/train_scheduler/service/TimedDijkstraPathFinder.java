package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.routing.RoutingConstraints;
import com.railways.train_scheduler.repository.TrackRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class TimedDijkstraPathFinder implements PathFinder {

    private final TrackRepository trackRepo;
    private final ReservationService reservationService;

    public TimedDijkstraPathFinder(TrackRepository trackRepo, ReservationService reservationService) {
        this.trackRepo = trackRepo;
        this.reservationService = reservationService;
    }

    @Override
    public Plan compute(Train train, RoutingConstraints constraints) {
        List<Track> edges = trackRepo.findAll();
        LocalDateTime startTime = train.getEarliestDeparture() != null ?
                LocalDateTime.parse(train.getEarliestDeparture()) : LocalDateTime.now();

        Plan plan = new Plan();
        plan.setTrainId(train.getId());
        plan.setTrainType(train.getType());

        List<RouteSegment> segments = findReroutedPath(train, edges, startTime);
        plan.setRoute(segments);
        plan.setCreatedAt(LocalDateTime.now());
        return plan;
    }

    private List<RouteSegment> findReroutedPath(Train train, List<Track> edges, LocalDateTime startTime) {
        int maxRetries = 5;
        LocalDateTime cursor = startTime;

        Map<String, List<Track>> adjList = new HashMap<>();
        for (Track t : edges) {
            if (t.getFromStation() != null) {
                adjList.computeIfAbsent(t.getFromStation(), k -> new ArrayList<>()).add(t);
            }
        }

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            List<Track> path = dijkstra(train, adjList, edges, cursor);
            if (path.isEmpty()) {
                cursor = cursor.plusMinutes(5);
                continue;
            }

            List<RouteSegment> segments = new ArrayList<>();
            boolean success = true;
            LocalDateTime segCursor = cursor;

            for (Track t : path) {
                int travelMinutes = t.getTravelTime() > 0 ? t.getTravelTime()
                        : (int) Math.max(1, Math.round((t.getDistanceKm() / (double) Math.max(1, t.getMaxSpeedKmph())) * 60));

                if (!reservationService.canReserve(train, t, segCursor, segCursor.plusMinutes(travelMinutes))) {
                    success = false;
                    break;
                }

                RouteSegment seg = new RouteSegment();
                seg.setSectionId(t.getId());
                seg.setStartTime(segCursor);
                seg.setEndTime(segCursor.plusMinutes(travelMinutes));
                segments.add(seg);
                segCursor = segCursor.plusMinutes(travelMinutes);
            }

            if (success) return segments;
            else cursor = cursor.plusMinutes(5);
        }

        RouteSegment hold = new RouteSegment();
        hold.setSectionId("HOLD_AT_" + (train.getSource() != null ? train.getSource() : "UNKNOWN"));
        hold.setStartTime(cursor);
        hold.setEndTime(cursor.plusMinutes(5));
        return Collections.singletonList(hold);
    }

    private static class QueueNode implements Comparable<QueueNode> {
        String id;
        int cost;
        int timeElapsed;

        QueueNode(String id, int cost, int timeElapsed) {
            this.id = id;
            this.cost = cost;
            this.timeElapsed = timeElapsed;
        }

        @Override
        public int compareTo(QueueNode o) {
            return Integer.compare(this.cost, o.cost);
        }
    }

    private List<Track> dijkstra(Train train, Map<String, List<Track>> adjList, List<Track> edges, LocalDateTime startTime) {
        Map<String, Integer> dist = new HashMap<>();
        Map<String, Track> prev = new HashMap<>();
        Set<String> visited = new HashSet<>();

        String source = train.getSource() != null ? train.getSource() : "";
        String dest = train.getDestination() != null ? train.getDestination() : "";

        if (source.isEmpty() || dest.isEmpty()) return Collections.emptyList();

        for (Track t : edges) {
            if (t.getFromStation() != null) dist.putIfAbsent(t.getFromStation(), Integer.MAX_VALUE);
            if (t.getToStation() != null) dist.putIfAbsent(t.getToStation(), Integer.MAX_VALUE);
        }
        dist.put(source, 0);

        PriorityQueue<QueueNode> pq = new PriorityQueue<>();
        pq.add(new QueueNode(source, 0, 0));

        while (!pq.isEmpty()) {
            QueueNode uNode = pq.poll();
            String u = uNode.id;
            
            if (!visited.add(u)) continue;
            if (u.equals(dest)) break;

            List<Track> neighbors = adjList.getOrDefault(u, Collections.emptyList());
            for (Track edge : neighbors) {
                int travelMinutes = edge.getTravelTime() > 0 ? edge.getTravelTime()
                        : (int) Math.max(1, Math.round((edge.getDistanceKm() / (double) Math.max(1, edge.getMaxSpeedKmph())) * 60));

                LocalDateTime edgeEnterTime = startTime.plusMinutes(uNode.timeElapsed);
                LocalDateTime edgeExitTime = edgeEnterTime.plusMinutes(travelMinutes);

                if (!reservationService.canReserve(train, edge, edgeEnterTime, edgeExitTime))
                    continue;

                int newCost = uNode.cost + travelMinutes * getPriorityFactor(train, edge);
                String to = edge.getToStation();
                if (newCost < dist.getOrDefault(to, Integer.MAX_VALUE)) {
                    dist.put(to, newCost);
                    prev.put(to, edge);
                    pq.add(new QueueNode(to, newCost, uNode.timeElapsed + travelMinutes));
                }
            }
        }

        List<Track> path = new ArrayList<>();
        String node = dest;
        while (prev.containsKey(node)) {
            Track edge = prev.get(node);
            path.add(edge);
            node = edge.getFromStation() != null ? edge.getFromStation() : "";
        }
        Collections.reverse(path);
        return path;
    }

    private int getPriorityFactor(Train train, Track track) {
        switch (train.getType()) {
            case "EXPRESS": return track.getTrackType() == Track.TrackType.FREIGHT ? 3 : 1;
            case "LOCAL": return track.getTrackType() == Track.TrackType.FREIGHT ? 3 : 1;
            case "FREIGHT": return track.getTrackType() == Track.TrackType.SINGLE ? 3 : 1;
            default: return 1;
        }
    }
}
