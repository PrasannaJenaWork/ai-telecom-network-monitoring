package com.telecom.monitoring.backend.alarm.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Alarm(
		UUID id,
		String nodeId,
		String alarmType,
		AlarmSeverity severity,
		String description,
		AlarmStatus status,
		Instant createdAt,
		Instant acknowledgedAt
) {

	public Alarm {
		Objects.requireNonNull(id, "id must not be null");
		nodeId = requireText(nodeId, "nodeId");
		alarmType = requireText(alarmType, "alarmType");
		Objects.requireNonNull(severity, "severity must not be null");
		description = requireText(description, "description");
		Objects.requireNonNull(status, "status must not be null");
		Objects.requireNonNull(createdAt, "createdAt must not be null");

		if (status == AlarmStatus.ACTIVE && acknowledgedAt != null) {
			throw new IllegalArgumentException("An active alarm cannot have an acknowledgment time");
		}
		if (status == AlarmStatus.ACKNOWLEDGED && acknowledgedAt == null) {
			throw new IllegalArgumentException("An acknowledged alarm must have an acknowledgment time");
		}
		if (acknowledgedAt != null && acknowledgedAt.isBefore(createdAt)) {
			throw new IllegalArgumentException("Acknowledgment time cannot precede creation time");
		}
	}

	public static Alarm create(
			UUID id,
			String nodeId,
			String alarmType,
			AlarmSeverity severity,
			String description,
			Instant createdAt
	) {
		return new Alarm(
				id,
				nodeId,
				alarmType,
				severity,
				description,
				AlarmStatus.ACTIVE,
				createdAt,
				null
		);
	}

	public Alarm acknowledge(Instant acknowledgmentTime) {
		if (status != AlarmStatus.ACTIVE) {
			throw new AlarmAlreadyAcknowledgedException(id);
		}

		return new Alarm(
				id,
				nodeId,
				alarmType,
				severity,
				description,
				AlarmStatus.ACKNOWLEDGED,
				createdAt,
				Objects.requireNonNull(acknowledgmentTime, "acknowledgmentTime must not be null")
		);
	}

	private static String requireText(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return value;
	}
}
