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

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TrackRepository trackRepository;
    private final TrainRepository trainRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              TrackRepository trackRepository,
                              TrainRepository trainRepository) {
        this.reservationRepository = reservationRepository;
        this.trackRepository = trackRepository;
        this.trainRepository = trainRepository;
    }

    public List<Reservation> findOverlapping(String trackId, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByTrackSectionIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(trackId, end, start);
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
            LocalDateTime end = seg.getEndTime().plusMinutes(accumulatedDelayMinutes);

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
                    created.add(reservationRepository.save(r));
                    
                    seg.setStartTime(start);
                    seg.setEndTime(end);
                    
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
    }

    public void holdTrainAt(Track track, Train train, LocalDateTime holdUntil) {
        train.setEarliestDeparture(holdUntil.toString());
    }
}
