package com.telecom.monitoring.backend.alarm.infrastructure.persistence;

import com.telecom.monitoring.backend.alarm.domain.Alarm;
import com.telecom.monitoring.backend.alarm.domain.AlarmAlreadyAcknowledgedException;
import com.telecom.monitoring.backend.alarm.domain.AlarmSeverity;
import com.telecom.monitoring.backend.alarm.domain.AlarmStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAlarmRepositoryTest {

	@Test
	void listsNewestFirstAndUsesTheIdAsATieBreaker() {
		InMemoryAlarmRepository repository = new InMemoryAlarmRepository();
		Instant older = Instant.parse("2026-10-10T09:00:00Z");
		Instant newer = Instant.parse("2026-10-10T10:00:00Z");
		UUID lowerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID higherId = UUID.fromString("00000000-0000-0000-0000-000000000002");
		UUID olderId = UUID.fromString("00000000-0000-0000-0000-000000000003");

		repository.save(alarm(olderId, older));
		repository.save(alarm(higherId, newer));
		repository.save(alarm(lowerId, newer));

		assertEquals(
				List.of(lowerId, higherId, olderId),
				repository.findAll().stream().map(Alarm::id).toList()
		);
	}

	@Test
	@Timeout(10)
	void allowsExactlyOneConcurrentAcknowledgment() throws Exception {
		InMemoryAlarmRepository repository = new InMemoryAlarmRepository();
		UUID alarmId = UUID.fromString("00000000-0000-0000-0000-000000000010");
		Instant createdAt = Instant.parse("2026-10-10T10:00:00Z");
		Instant acknowledgedAt = createdAt.plusSeconds(30);
		repository.save(alarm(alarmId, createdAt));

		int operationCount = 12;
		CountDownLatch ready = new CountDownLatch(operationCount);
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(operationCount);
		List<Future<Boolean>> results = new ArrayList<>();

		try {
			for (int i = 0; i < operationCount; i++) {
				results.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					try {
						repository.updateAtomically(alarmId, alarm -> alarm.acknowledge(acknowledgedAt));
						return true;
					} catch (AlarmAlreadyAcknowledgedException exception) {
						return false;
					}
				}));
			}

			assertTrue(ready.await(5, TimeUnit.SECONDS));
			start.countDown();

			long successfulAcknowledgments = 0;
			for (Future<Boolean> result : results) {
				if (result.get()) {
					successfulAcknowledgments++;
				}
			}

			assertEquals(1, successfulAcknowledgments);
			Alarm stored = repository.findById(alarmId).orElseThrow();
			assertEquals(AlarmStatus.ACKNOWLEDGED, stored.status());
			assertEquals(acknowledgedAt, stored.acknowledgedAt());
		} finally {
			start.countDown();
			executor.shutdownNow();
		}
	}

	private Alarm alarm(UUID id, Instant createdAt) {
		return Alarm.create(
				id,
				"RAN-SE-001",
				"HIGH_PACKET_LOSS",
				AlarmSeverity.MAJOR,
				"Packet loss exceeded threshold",
				createdAt
		);
	}
}
