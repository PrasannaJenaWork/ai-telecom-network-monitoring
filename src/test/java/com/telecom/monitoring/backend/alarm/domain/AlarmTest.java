package com.telecom.monitoring.backend.alarm.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlarmTest {

	private static final UUID ALARM_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-10-10T10:00:00Z");

	@Test
	void createsAnActiveAlarm() {
		Alarm alarm = activeAlarm();

		assertEquals(AlarmStatus.ACTIVE, alarm.status());
		assertEquals(CREATED_AT, alarm.createdAt());
		assertNull(alarm.acknowledgedAt());
	}

	@Test
	void acknowledgesAnAlarmThroughAnExplicitTransition() {
		Instant acknowledgedAt = CREATED_AT.plusSeconds(30);

		Alarm acknowledged = activeAlarm().acknowledge(acknowledgedAt);

		assertEquals(AlarmStatus.ACKNOWLEDGED, acknowledged.status());
		assertEquals(acknowledgedAt, acknowledged.acknowledgedAt());
	}

	@Test
	void rejectsARepeatedAcknowledgment() {
		Alarm acknowledged = activeAlarm().acknowledge(CREATED_AT.plusSeconds(30));

		assertThrows(
				AlarmAlreadyAcknowledgedException.class,
				() -> acknowledged.acknowledge(CREATED_AT.plusSeconds(60))
		);
	}

	@Test
	void rejectsAnAcknowledgmentBeforeCreation() {
		assertThrows(
				IllegalArgumentException.class,
				() -> activeAlarm().acknowledge(CREATED_AT.minusSeconds(1))
		);
	}

	private Alarm activeAlarm() {
		return Alarm.create(
				ALARM_ID,
				"RAN-SE-001",
				"HIGH_PACKET_LOSS",
				AlarmSeverity.MAJOR,
				"Packet loss exceeded threshold",
				CREATED_AT
		);
	}
}
