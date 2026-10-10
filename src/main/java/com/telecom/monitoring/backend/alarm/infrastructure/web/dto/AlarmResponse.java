package com.telecom.monitoring.backend.alarm.infrastructure.web.dto;

import com.telecom.monitoring.backend.alarm.domain.Alarm;
import com.telecom.monitoring.backend.alarm.domain.AlarmSeverity;
import com.telecom.monitoring.backend.alarm.domain.AlarmStatus;

import java.time.Instant;
import java.util.UUID;

public record AlarmResponse(
		UUID id,
		String nodeId,
		String alarmType,
		AlarmSeverity severity,
		String description,
		AlarmStatus status,
		Instant createdAt,
		Instant acknowledgedAt
) {

	public static AlarmResponse from(Alarm alarm) {
		return new AlarmResponse(
				alarm.id(),
				alarm.nodeId(),
				alarm.alarmType(),
				alarm.severity(),
				alarm.description(),
				alarm.status(),
				alarm.createdAt(),
				alarm.acknowledgedAt()
		);
	}
}
