package com.railways.train_scheduler;

import com.railways.train_scheduler.repository.EventRepository;
import com.railways.train_scheduler.repository.KPIRepository;
import com.railways.train_scheduler.repository.LiveStateRepository;
import com.railways.train_scheduler.repository.PlanRepository;
import com.railways.train_scheduler.repository.ReservationRepository;
import com.railways.train_scheduler.repository.TrackRepository;
import com.railways.train_scheduler.repository.TrainRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class TrainSchedulerApplicationTests {

	@MockBean
	private EventRepository eventRepository;

	@MockBean
	private KPIRepository kpiRepository;

	@MockBean
	private LiveStateRepository liveStateRepository;

	@MockBean
	private PlanRepository planRepository;

	@MockBean
	private ReservationRepository reservationRepository;

	@MockBean
	private TrackRepository trackRepository;

	@MockBean
	private TrainRepository trainRepository;

	@Test
	void contextLoads() {
	}

}
