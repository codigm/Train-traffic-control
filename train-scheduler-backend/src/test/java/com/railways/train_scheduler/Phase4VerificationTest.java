package com.railways.train_scheduler;

import com.railways.train_scheduler.model.core.Track;
import com.railways.train_scheduler.model.core.Train;
import com.railways.train_scheduler.model.planning.Plan;
import com.railways.train_scheduler.model.scheduling.Reservation;
import com.railways.train_scheduler.model.scheduling.RouteSegment;
import com.railways.train_scheduler.repository.ReservationRepository;
import com.railways.train_scheduler.repository.TrackRepository;
import com.railways.train_scheduler.repository.TrainRepository;
import com.railways.train_scheduler.service.ReservationService;
import com.railways.train_scheduler.service.TimedDijkstraPathFinder;
import com.railways.train_scheduler.service.ai.AITravelTimePredictor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Phase4VerificationTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TrackRepository trackRepository;
    @Mock
    private TrainRepository trainRepository;

    private ReservationService reservationService;

    @Mock
    private ReservationService mockReservationService;
    @Mock
    private AITravelTimePredictor travelTimePredictor;

    private TimedDijkstraPathFinder pathFinder;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, trackRepository, trainRepository);
        pathFinder = new TimedDijkstraPathFinder(trackRepository, mockReservationService, travelTimePredictor);
    }

    @Test
    void testSingleTrackBidirectionalLocking() {
        Track trackAtoB = new Track();
        trackAtoB.setId("track1");
        trackAtoB.setFromStation("A");
        trackAtoB.setToStation("B");
        trackAtoB.setTrackType(Track.TrackType.SINGLE);
        trackAtoB.setCapacity(1);

        Track trackBtoA = new Track();
        trackBtoA.setId("track2");
        trackBtoA.setFromStation("B");
        trackBtoA.setToStation("A");
        trackBtoA.setTrackType(Track.TrackType.SINGLE);
        trackBtoA.setCapacity(1);

        Train t1 = new Train();
        t1.setId("T1");
        t1.setType("EXPRESS");

        Train t2 = new Train();
        t2.setId("T2");
        t2.setType("EXPRESS");

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(10);

        Reservation r = new Reservation();
        r.setTrackSectionId(trackBtoA.getId());
        r.setTrainId(t2.getId());
        r.setStartTime(start);
        r.setEndTime(end);

        when(reservationRepository.findAll()).thenReturn(List.of(r));
        when(trackRepository.findByFromStationAndToStation("B", "A")).thenReturn(Optional.of(trackBtoA));
        
        reservationService.loadCache();

        boolean canReserve = reservationService.canReserve(t1, trackAtoB, start.plusMinutes(2), end.minusMinutes(2));

        assertFalse(canReserve, "Should not be able to reserve SINGLE track if reverse track has a conflicting reservation");
    }

    @Test
    void testWaitVsRerouteLogic() {
        Track trackAtoB = new Track();
        trackAtoB.setId("track1");
        trackAtoB.setFromStation("A");
        trackAtoB.setToStation("B");
        trackAtoB.setTrackType(Track.TrackType.DOUBLE);
        trackAtoB.setCapacity(1);
        trackAtoB.setOperational(true);

        when(trackRepository.findAll()).thenReturn(List.of(trackAtoB));

        Train t1 = new Train();
        t1.setId("T1");
        t1.setSource("A");
        t1.setDestination("B");
        t1.setEarliestDeparture(LocalDateTime.now().toString());

        when(travelTimePredictor.predictTravelTimeMinutes(eq(trackAtoB), any(LocalDateTime.class), any())).thenReturn(10);

        // First 2 attempts fail (track is busy), 3rd attempt succeeds -> 10 mins wait
        when(mockReservationService.canReserve(eq(t1), eq(trackAtoB), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        Plan plan = pathFinder.compute(t1, null);

        assertNotNull(plan.getRoute());
        assertEquals(1, plan.getRoute().size());

        RouteSegment segment = plan.getRoute().get(0);
        
        // Wait should be 10 mins, travel time 10 mins. 
        // 10 mins added to start time, so duration between original start and final end is 20.
        // The segment start time gets pushed by 10 mins (2 attempts * 5 mins).
        LocalDateTime expectedStart = LocalDateTime.parse(t1.getEarliestDeparture()).plusMinutes(10);
        assertEquals(expectedStart, segment.getStartTime());
        assertEquals(expectedStart.plusMinutes(10), segment.getEndTime());
    }

    @Test
    void testAIRoutingWeatherCongestion() {
        Track trackAtoB_Fast = new Track();
        trackAtoB_Fast.setId("track1");
        trackAtoB_Fast.setFromStation("A");
        trackAtoB_Fast.setToStation("B");
        trackAtoB_Fast.setOperational(true);

        Track trackAtoC = new Track();
        trackAtoC.setId("track2");
        trackAtoC.setFromStation("A");
        trackAtoC.setToStation("C");
        trackAtoC.setOperational(true);

        Track trackCtoB = new Track();
        trackCtoB.setId("track3");
        trackCtoB.setFromStation("C");
        trackCtoB.setToStation("B");
        trackCtoB.setOperational(true);

        when(trackRepository.findAll()).thenReturn(Arrays.asList(trackAtoB_Fast, trackAtoC, trackCtoB));

        Train t1 = new Train();
        t1.setId("T1");
        t1.setSource("A");
        t1.setDestination("B");
        t1.setEarliestDeparture(LocalDateTime.now().toString());

        when(mockReservationService.canReserve(eq(t1), any(Track.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        // Under normal weather A->B is 50 mins, but under bad weather A->B is 120 mins
        // A->C is 30, C->B is 30 (Total 60 mins). A->B should be avoided if weather/congestion makes it 120
        when(travelTimePredictor.predictTravelTimeMinutes(eq(trackAtoB_Fast), any(LocalDateTime.class), any()))
                .thenReturn(120);
        when(travelTimePredictor.predictTravelTimeMinutes(eq(trackAtoC), any(LocalDateTime.class), any()))
                .thenReturn(30);
        when(travelTimePredictor.predictTravelTimeMinutes(eq(trackCtoB), any(LocalDateTime.class), any()))
                .thenReturn(30);

        Plan plan = pathFinder.compute(t1, null);

        assertNotNull(plan.getRoute());
        assertEquals(2, plan.getRoute().size()); // It should route through C instead of direct B
        assertEquals("track2", plan.getRoute().get(0).getSectionId());
        assertEquals("track3", plan.getRoute().get(1).getSectionId());
    }
}
