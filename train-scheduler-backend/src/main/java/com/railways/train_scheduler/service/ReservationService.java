package com.railways.train_scheduler.service;

import com.railways.train_scheduler.model.scheduling.Reservation;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.repository.ReservationRepository;
import com.railways.train_scheduler.repository.TrackRepository;
import com.railways.train_scheduler.repository.TrainRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.annotation.PostConstruct;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrackRepository trackRepository;
    private final TrainRepository trainRepository;

    private final Map<String, List<Reservation>> reservationCache = new ConcurrentHashMap<>();

    public ReservationService(ReservationRepository reservationRepository,
                              TrackRepository trackRepository,
                              TrainRepository trainRepository) {
        this.reservationRepository = reservationRepository;
        this.trackRepository = trackRepository;
        this.trainRepository = trainRepository;
    }

    @PostConstruct
    public void loadCache() {
        reservationCache.clear();
        for (Reservation r : reservationRepository.findAll()) {
            reservationCache.computeIfAbsent(r.getTrackSectionId(), k -> new ArrayList<>()).add(r);
        }
    }

    public List<Reservation> findOverlapping(String trackId, LocalDateTime start, LocalDateTime end) {
        List<Reservation> all = reservationCache.getOrDefault(trackId, new ArrayList<>());
        List<Reservation> overlaps = new ArrayList<>();
        for (Reservation r : all) {
            // Note: End time overlaps if r.startTime < end && r.endTime > start
            if (r.getStartTime().isBefore(end) && r.getEndTime().isAfter(start)) {
                overlaps.add(r);
            }
        }
        return overlaps;
    }

    public int getCapacity(@NonNull String trackId) {
        Track sec = trackRepository.findById(trackId).orElse(null);
        return sec != null ? Math.max(1, sec.getCapacity()) : 1;
    }

    public boolean canReserve(Train train, Track track, LocalDateTime start, LocalDateTime end) {
        List<Reservation> overlaps = findOverlapping(track.getId(), start, end);
        for (Reservation r : overlaps) {
            Train other = trainRepository.findById(Objects.requireNonNull(r.getTrainId())).orElse(null);
            if (other != null && isHigherPriority(other, train)) {
                return false; // must wait for higher-priority train
            }
        }
        // Also check if reverse track is locked for SINGLE tracks
        if (track.getTrackType() == Track.TrackType.SINGLE) {
            Track reverseTrack = trackRepository.findByFromStationAndToStation(track.getToStation(), track.getFromStation()).orElse(null);
            if (reverseTrack != null) {
                List<Reservation> reverseOverlaps = findOverlapping(reverseTrack.getId(), start, end);
                if (!reverseOverlaps.isEmpty()) {
                    return false; // Cannot reserve if a train is coming head-on
                }
            }
        }

        return overlaps.size() < track.getCapacity();
    }

    private boolean isHigherPriority(Train t1, Train t2) {
        return getPriorityValue(t1) > getPriorityValue(t2);
    }

    private int getPriorityValue(Train t) {
        switch (t.getType()) {
            case "EXPRESS": return 3;
            case "LOCAL": return 2;
            case "FREIGHT": return 1;
            default: return 0;
        }
    }

    public void reserveMany(String planId, String trainId, List<RouteSegment> segments) {
        List<Reservation> created = new ArrayList<>();
        int accumulatedDelayMinutes = 0;

        for (RouteSegment seg : segments) {
            Track track = trackRepository.findById(Objects.requireNonNull(seg.getSectionId())).orElse(null);
            if (track == null) continue;

            LocalDateTime start = seg.getStartTime().plusMinutes(accumulatedDelayMinutes);
            // Add safeGapTime to the end to ensure headway buffer
            LocalDateTime end = seg.getEndTime().plusMinutes(accumulatedDelayMinutes).plusMinutes(track.getSafeGapTimeMinutes());

            boolean reserved = false;
            int maxRetries = 5;

            for (int attempt = 0; attempt < maxRetries; attempt++) {
                if (canReserve(trainRepository.findById(Objects.requireNonNull(trainId)).orElse(null), track, start, end)) {
                    Reservation r = new Reservation();
                    r.setTrackSectionId(track.getId());
                    r.setTrainId(trainId);
                    r.setStartTime(start);
                    r.setEndTime(end);
                    r.setPlanId(planId);
                    Reservation saved = reservationRepository.save(r);
                    created.add(saved);
                    reservationCache.computeIfAbsent(track.getId(), k -> new ArrayList<>()).add(saved);

                    // Add reverse lock for SINGLE track
                    if (track.getTrackType() == Track.TrackType.SINGLE) {
                        Track reverseTrack = trackRepository.findByFromStationAndToStation(track.getToStation(), track.getFromStation()).orElse(null);
                        if (reverseTrack != null) {
                            Reservation rr = new Reservation();
                            rr.setTrackSectionId(reverseTrack.getId());
                            rr.setTrainId(trainId);
                            rr.setStartTime(start);
                            rr.setEndTime(end);
                            rr.setPlanId(planId);
                            Reservation savedRev = reservationRepository.save(rr);
                            created.add(savedRev);
                            reservationCache.computeIfAbsent(reverseTrack.getId(), k -> new ArrayList<>()).add(savedRev);
                        }
                    }
                    
                    seg.setStartTime(start);
                    seg.setEndTime(end.minusMinutes(track.getSafeGapTimeMinutes())); // segment actual exit time doesn't include gap
                    
                    reserved = true;
                    break;
                } else {
                    start = start.plusMinutes(5);
                    end = end.plusMinutes(5);
                    accumulatedDelayMinutes += 5;
                }
            }

            if (!reserved) {
                System.out.println("Train " + trainId + " holding before section " + track.getId());
                throw new RuntimeException("Track capacity full. Train " + trainId + " cannot reserve track " + track.getId());
            }
        }
    }

    public void cancelReservationsForPlan(String planId) {
        List<Reservation> list = reservationRepository.findByPlanId(planId);
        reservationRepository.deleteAll(Objects.requireNonNull(list));
        loadCache(); // Re-sync cache after deletion
    }

    public void holdTrainAt(Track track, Train train, LocalDateTime holdUntil) {
        train.setEarliestDeparture(holdUntil.toString());
    }
}
