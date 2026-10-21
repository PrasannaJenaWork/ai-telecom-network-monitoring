package com.telecom.monitoring.backend.alarm.application.service;

import com.telecom.monitoring.backend.alarm.application.exception.AlarmNotFoundException;
import com.telecom.monitoring.backend.alarm.application.port.in.CreateAlarmCommand;
import com.telecom.monitoring.backend.alarm.domain.Alarm;
import com.telecom.monitoring.backend.alarm.domain.AlarmAlreadyAcknowledgedException;
import com.telecom.monitoring.backend.alarm.domain.AlarmSeverity;
import com.telecom.monitoring.backend.alarm.domain.AlarmStatus;
import com.telecom.monitoring.backend.alarm.infrastructure.persistence.InMemoryAlarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-10T10:00:00Z");

	private AlarmService alarmService;

	@BeforeEach
	void setUp() {
		alarmService = new AlarmService(
				new InMemoryAlarmRepository(),
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void createsListsGetsAndAcknowledgesAnAlarm() {
		Alarm created = alarmService.createAlarm(new CreateAlarmCommand(
				"CORE-SE-001",
				"CPU_SATURATION",
				AlarmSeverity.CRITICAL,
				"CPU saturation exceeded threshold"
		));

		assertEquals(AlarmStatus.ACTIVE, created.status());
		assertEquals(NOW, created.createdAt());
		assertEquals(created, alarmService.getAlarm(created.id()));
		assertEquals(1, alarmService.listAlarms().size());

		Alarm acknowledged = alarmService.acknowledgeAlarm(created.id());
		assertEquals(AlarmStatus.ACKNOWLEDGED, acknowledged.status());
		assertEquals(NOW, acknowledged.acknowledgedAt());
		assertThrows(AlarmAlreadyAcknowledgedException.class, () -> alarmService.acknowledgeAlarm(created.id()));
	}

	@Test
	void reportsMissingAlarms() {
		UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000099");

		assertThrows(AlarmNotFoundException.class, () -> alarmService.getAlarm(missingId));
		assertThrows(AlarmNotFoundException.class, () -> alarmService.acknowledgeAlarm(missingId));
	}
}
